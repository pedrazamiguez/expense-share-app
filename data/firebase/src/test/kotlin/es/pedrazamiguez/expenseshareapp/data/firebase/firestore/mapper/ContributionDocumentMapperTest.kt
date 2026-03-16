package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ContributionDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContributionDocumentMapperTest {

    private val testContributionId = "contrib-123"
    private val testGroupId = "group-456"
    private val testUserId = "user-789"
    private val testSubunitId = "subunit-101"
    private val testGroupDocRef: DocumentReference = mockk(relaxed = true)
    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val testFirebaseTimestamp = testTimestamp.toTimestampUtc()!!

    private val fullContribution = Contribution(
        id = testContributionId,
        groupId = testGroupId,
        userId = testUserId,
        subunitId = testSubunitId,
        amount = 50000L,
        currency = "EUR",
        createdAt = testTimestamp,
        lastUpdatedAt = testTimestamp
    )

    @Nested
    inner class ToDocument {

        @Test
        fun `maps all core fields correctly`() {
            val document = fullContribution.toDocument(
                testContributionId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertEquals(testContributionId, document.contributionId)
            assertEquals(testGroupId, document.groupId)
            assertEquals(testGroupDocRef, document.groupRef)
            assertEquals(testUserId, document.userId)
            assertEquals(testSubunitId, document.subunitId)
            assertEquals(50000L, document.amountCents)
            assertEquals("EUR", document.currency)
            assertEquals(testUserId, document.createdBy)
        }

        @Test
        fun `maps null subunitId correctly`() {
            val contributionNoSubunit = fullContribution.copy(subunitId = null)

            val document = contributionNoSubunit.toDocument(
                testContributionId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            assertNull(document.subunitId)
        }

        @Test
        fun `maps createdAt and lastUpdatedAt when present`() {
            val document = fullContribution.toDocument(
                testContributionId,
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
            val contributionNoTimestamps = fullContribution.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val document = contributionNoTimestamps.toDocument(
                testContributionId,
                testGroupId,
                testGroupDocRef,
                testUserId
            )

            // The mapper uses LocalDateTime.now() as fallback, so timestamps must be non-null
            assertNotNull(document.createdAt)
            assertNotNull(document.lastUpdatedAt)
        }
    }

    @Nested
    inner class ToDomain {

        private val fullDocument = ContributionDocument(
            contributionId = testContributionId,
            groupId = testGroupId,
            userId = testUserId,
            subunitId = testSubunitId,
            amountCents = 50000L,
            currency = "EUR",
            createdBy = testUserId,
            createdAt = testFirebaseTimestamp,
            lastUpdatedAt = testFirebaseTimestamp
        )

        @Test
        fun `maps all core fields correctly`() {
            val contribution = fullDocument.toDomain()

            assertEquals(testContributionId, contribution.id)
            assertEquals(testGroupId, contribution.groupId)
            assertEquals(testUserId, contribution.userId)
            assertEquals(testSubunitId, contribution.subunitId)
            assertEquals(50000L, contribution.amount)
            assertEquals("EUR", contribution.currency)
        }

        @Test
        fun `maps null subunitId correctly`() {
            val documentNoSubunit = fullDocument.copy(subunitId = null)

            val contribution = documentNoSubunit.toDomain()

            assertNull(contribution.subunitId)
        }

        @Test
        fun `maps timestamps correctly`() {
            val contribution = fullDocument.toDomain()

            assertEquals(testTimestamp, contribution.createdAt)
            assertEquals(testTimestamp, contribution.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null domain fields`() {
            val documentNullTimestamps = fullDocument.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val contribution = documentNullTimestamps.toDomain()

            assertNull(contribution.createdAt)
            assertNull(contribution.lastUpdatedAt)
        }
    }
}
