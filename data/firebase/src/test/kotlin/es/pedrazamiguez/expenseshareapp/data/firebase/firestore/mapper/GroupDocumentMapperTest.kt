package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GroupDocumentMapperTest {

    private val testGroupId = "group-123"
    private val testUserId = "user-456"
    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val testFirebaseTimestamp = testTimestamp.toTimestampUtc()!!

    private val fullGroup = Group(
        id = testGroupId,
        name = "Trip to Japan",
        description = "Travel expenses",
        currency = "EUR",
        extraCurrencies = listOf("JPY", "THB"),
        members = listOf("user-1", "user-2", "user-3"),
        mainImagePath = "/images/japan.jpg",
        createdAt = testTimestamp,
        lastUpdatedAt = testTimestamp
    )

    @Nested
    inner class ToDocument {

        @Test
        fun `maps all core fields correctly`() {
            val document = fullGroup.toDocument(testGroupId, testUserId)

            assertEquals(testGroupId, document.groupId)
            assertEquals("Trip to Japan", document.name)
            assertEquals("Travel expenses", document.description)
            assertEquals("EUR", document.currency)
            assertEquals(testUserId, document.createdBy)
        }

        @Test
        fun `maps extraCurrencies list`() {
            val document = fullGroup.toDocument(testGroupId, testUserId)

            assertEquals(listOf("JPY", "THB"), document.extraCurrencies)
        }

        @Test
        fun `maps memberIds list`() {
            val document = fullGroup.toDocument(testGroupId, testUserId)

            assertEquals(listOf("user-1", "user-2", "user-3"), document.memberIds)
        }

        @Test
        fun `maps createdAt and lastUpdatedAt when present`() {
            val document = fullGroup.toDocument(testGroupId, testUserId)

            assertNotNull(document.createdAt)
            assertNotNull(document.lastUpdatedAt)
            assertEquals(testFirebaseTimestamp, document.createdAt)
            assertEquals(testFirebaseTimestamp, document.lastUpdatedAt)
        }

        @Test
        fun `createdAt and lastUpdatedAt are null when domain timestamps are null`() {
            val groupWithoutTimestamps = fullGroup.copy(createdAt = null, lastUpdatedAt = null)

            val document = groupWithoutTimestamps.toDocument(testGroupId, testUserId)

            assertNull(document.createdAt)
            assertNull(document.lastUpdatedAt)
        }

        @Test
        fun `maps empty lists correctly`() {
            val groupEmptyLists = fullGroup.copy(
                extraCurrencies = emptyList(),
                members = emptyList()
            )

            val document = groupEmptyLists.toDocument(testGroupId, testUserId)

            assertTrue(document.extraCurrencies.isEmpty())
            assertTrue(document.memberIds.isEmpty())
        }
    }

    @Nested
    inner class ToDomain {

        private val fullDocument = GroupDocument(
            groupId = testGroupId,
            name = "Trip to Japan",
            description = "Travel expenses",
            currency = "EUR",
            extraCurrencies = listOf("JPY", "THB"),
            memberIds = listOf("user-1", "user-2", "user-3"),
            mainImagePath = "/images/japan.jpg",
            createdBy = testUserId,
            createdAt = testFirebaseTimestamp,
            lastUpdatedAt = testFirebaseTimestamp
        )

        @Test
        fun `maps all core fields correctly`() {
            val group = fullDocument.toDomain()

            assertEquals(testGroupId, group.id)
            assertEquals("Trip to Japan", group.name)
            assertEquals("Travel expenses", group.description)
            assertEquals("EUR", group.currency)
            assertEquals("/images/japan.jpg", group.mainImagePath)
        }

        @Test
        fun `maps lists correctly`() {
            val group = fullDocument.toDomain()

            assertEquals(listOf("JPY", "THB"), group.extraCurrencies)
            assertEquals(listOf("user-1", "user-2", "user-3"), group.members)
        }

        @Test
        fun `maps timestamps correctly`() {
            val group = fullDocument.toDomain()

            assertEquals(testTimestamp, group.createdAt)
            assertEquals(testTimestamp, group.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null domain fields`() {
            val documentNullTimestamps = fullDocument.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val group = documentNullTimestamps.toDomain()

            assertNull(group.createdAt)
            assertNull(group.lastUpdatedAt)
        }

        @Test
        fun `maps empty lists correctly`() {
            val documentEmptyLists = fullDocument.copy(
                extraCurrencies = emptyList(),
                memberIds = emptyList()
            )

            val group = documentEmptyLists.toDomain()

            assertTrue(group.extraCurrencies.isEmpty())
            assertTrue(group.members.isEmpty())
        }
    }
}

