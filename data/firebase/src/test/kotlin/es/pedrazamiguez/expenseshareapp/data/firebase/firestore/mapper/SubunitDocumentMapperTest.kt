package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.SubunitDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SubunitDocumentMapperTest {

    private val testSubunitId = "subunit-123"
    private val testGroupId = "group-456"
    private val testUserId = "user-789"
    private val testGroupDocRef: DocumentReference = mockk(relaxed = true)
    private val testTimestamp = LocalDateTime.of(2026, 3, 13, 12, 30, 0)
    private val testFirebaseTimestamp = testTimestamp.toTimestampUtc()!!
    private val testName = "Antonio & Me"
    private val testMemberIds = listOf("user-789", "user-012")
    /** Domain model uses BigDecimal */
    private val testMemberSharesDomain = mapOf("user-789" to BigDecimal("0.5"), "user-012" to BigDecimal("0.5"))
    /** Firestore document uses String */
    private val testMemberSharesDoc = mapOf("user-789" to "0.5", "user-012" to "0.5")

    private val fullSubunit = Subunit(
        id = testSubunitId,
        groupId = testGroupId,
        name = testName,
        memberIds = testMemberIds,
        memberShares = testMemberSharesDomain,
        createdBy = testUserId,
        createdAt = testTimestamp,
        lastUpdatedAt = testTimestamp
    )

    @Nested
    inner class ToDocument {

        @Test
        fun `maps all core fields correctly`() {
            val document = fullSubunit.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertEquals(testSubunitId, document.subunitId)
            assertEquals(testGroupId, document.groupId)
            assertEquals(testGroupDocRef, document.groupRef)
            assertEquals(testName, document.name)
            assertEquals(testMemberIds, document.memberIds)
            assertEquals(testMemberSharesDoc, document.memberShares)
            assertEquals(testUserId, document.createdBy)
        }

        @Test
        fun `maps createdAt and lastUpdatedAt when present`() {
            val document = fullSubunit.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertNotNull(document.createdAt)
            assertNotNull(document.lastUpdatedAt)
            assertEquals(testFirebaseTimestamp, document.createdAt)
            assertEquals(testFirebaseTimestamp, document.lastUpdatedAt)
        }

        @Test
        fun `falls back to LocalDateTime now when timestamps are null`() {
            val subunitNoTimestamps = fullSubunit.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val document = subunitNoTimestamps.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertNotNull(document.createdAt)
            assertNotNull(document.lastUpdatedAt)
        }

        @Test
        fun `handles empty memberIds and memberShares`() {
            val subunit = fullSubunit.copy(
                memberIds = emptyList(),
                memberShares = emptyMap()
            )

            val document = subunit.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertTrue(document.memberIds.isEmpty())
            assertTrue(document.memberShares.isEmpty())
        }

        @Test
        fun `maps memberShares with varying weights converting BigDecimal to String`() {
            val subunit = fullSubunit.copy(
                memberIds = listOf("u1", "u2", "u3"),
                memberShares = mapOf("u1" to BigDecimal("0.4"), "u2" to BigDecimal("0.3"), "u3" to BigDecimal("0.3"))
            )

            val document = subunit.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertEquals(3, document.memberShares.size)
            assertEquals("0.4", document.memberShares["u1"])
            assertEquals("0.3", document.memberShares["u2"])
            assertEquals("0.3", document.memberShares["u3"])
        }

        @Test
        fun `preserves createdBy when not blank`() {
            val subunit = fullSubunit.copy(createdBy = "original-creator")

            val document = subunit.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertEquals("original-creator", document.createdBy)
        }

        @Test
        fun `falls back to userId when createdBy is blank`() {
            val subunit = fullSubunit.copy(createdBy = "")

            val document = subunit.toDocument(
                testSubunitId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertEquals(testUserId, document.createdBy)
        }
    }

    @Nested
    inner class ToDomain {

        private val fullDocument = SubunitDocument(
            subunitId = testSubunitId,
            groupId = testGroupId,
            groupRef = testGroupDocRef,
            name = testName,
            memberIds = testMemberIds,
            memberShares = testMemberSharesDoc,
            createdBy = testUserId,
            createdAt = testFirebaseTimestamp,
            lastUpdatedAt = testFirebaseTimestamp
        )

        @Test
        fun `maps all core fields correctly converting String to BigDecimal`() {
            val subunit = fullDocument.toDomain()

            assertEquals(testSubunitId, subunit.id)
            assertEquals(testGroupId, subunit.groupId)
            assertEquals(testName, subunit.name)
            assertEquals(testMemberIds, subunit.memberIds)
            assertEquals(2, subunit.memberShares.size)
            assertEquals(0, BigDecimal("0.5").compareTo(subunit.memberShares["user-789"]))
            assertEquals(0, BigDecimal("0.5").compareTo(subunit.memberShares["user-012"]))
            assertEquals(testUserId, subunit.createdBy)
        }

        @Test
        fun `maps timestamps correctly`() {
            val subunit = fullDocument.toDomain()

            assertEquals(testTimestamp, subunit.createdAt)
            assertEquals(testTimestamp, subunit.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null domain fields`() {
            val documentNullTimestamps = fullDocument.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val subunit = documentNullTimestamps.toDomain()

            assertNull(subunit.createdAt)
            assertNull(subunit.lastUpdatedAt)
        }

        @Test
        fun `handles empty memberIds and memberShares`() {
            val document = fullDocument.copy(
                memberIds = emptyList(),
                memberShares = emptyMap()
            )

            val subunit = document.toDomain()

            assertTrue(subunit.memberIds.isEmpty())
            assertTrue(subunit.memberShares.isEmpty())
        }

        @Test
        fun `falls back to BigDecimal ZERO for non-numeric memberShares values`() {
            val document = fullDocument.copy(
                memberShares = mapOf(
                    "user-789" to "0.5",
                    "user-012" to "not-a-number"
                )
            )

            val subunit = document.toDomain()

            assertEquals(0, BigDecimal("0.5").compareTo(subunit.memberShares["user-789"]))
            assertEquals(0, BigDecimal.ZERO.compareTo(subunit.memberShares["user-012"]))
        }

        @Test
        fun `falls back to BigDecimal ZERO for empty string memberShares values`() {
            val document = fullDocument.copy(
                memberShares = mapOf("user-789" to "")
            )

            val subunit = document.toDomain()

            assertEquals(0, BigDecimal.ZERO.compareTo(subunit.memberShares["user-789"]))
        }
    }

    @Nested
    inner class ListMapping {

        @Test
        fun `toDomainSubunits maps all elements with String to BigDecimal conversion`() {
            val documents = listOf(
                SubunitDocument(
                    subunitId = "sub-1",
                    groupId = testGroupId,
                    name = "Couple A",
                    memberIds = listOf("u1", "u2"),
                    memberShares = mapOf("u1" to "0.5", "u2" to "0.5"),
                    createdBy = "u1",
                    createdAt = testFirebaseTimestamp,
                    lastUpdatedAt = testFirebaseTimestamp
                ),
                SubunitDocument(
                    subunitId = "sub-2",
                    groupId = testGroupId,
                    name = "Family B",
                    memberIds = listOf("u3", "u4", "u5"),
                    memberShares = mapOf("u3" to "0.4", "u4" to "0.3", "u5" to "0.3"),
                    createdBy = "u3",
                    createdAt = testFirebaseTimestamp,
                    lastUpdatedAt = testFirebaseTimestamp
                )
            )

            val subunits = documents.toDomainSubunits()

            assertEquals(2, subunits.size)
            assertEquals("sub-1", subunits[0].id)
            assertEquals("Couple A", subunits[0].name)
            assertEquals(0, BigDecimal("0.5").compareTo(subunits[0].memberShares["u1"]))
            assertEquals("sub-2", subunits[1].id)
            assertEquals("Family B", subunits[1].name)
            assertEquals(0, BigDecimal("0.4").compareTo(subunits[1].memberShares["u3"]))
        }
    }
}
