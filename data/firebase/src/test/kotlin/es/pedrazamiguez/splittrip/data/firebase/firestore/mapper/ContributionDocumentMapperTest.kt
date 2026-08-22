package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import es.pedrazamiguez.splittrip.data.firebase.firestore.document.ContributionDocument
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import java.math.BigDecimal
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
    private val testActorId = "actor-111"
    private val testSubunitId = "subunit-101"
    private val testLinkedExpenseId = "expense-202"
    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val testFirebaseTimestamp = testTimestamp.toTimestampUtc()!!

    private val fullContribution = Contribution(
        id = testContributionId,
        groupId = testGroupId,
        userId = testUserId,
        createdBy = testActorId,
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
                testActorId
            )

            assertEquals(testContributionId, document.contributionId)
            assertEquals(testGroupId, document.groupId)
            assertEquals(testUserId, document.userId)
            assertEquals(testSubunitId, document.subunitId)
            assertEquals(50000L, document.amountCents)
            assertEquals("EUR", document.currency)
            assertEquals(testActorId, document.createdBy)
        }

        @Test
        fun `preserves userId from domain model when not blank`() {
            val document = fullContribution.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals(testUserId, document.userId)
        }

        @Test
        fun `falls back to userId param when domain userId is blank`() {
            val contributionBlankUser = fullContribution.copy(userId = "")

            val document = contributionBlankUser.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals(testActorId, document.userId)
        }

        @Test
        fun `preserves createdBy from domain model when not blank`() {
            val document = fullContribution.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals(testActorId, document.createdBy)
        }

        @Test
        fun `falls back to userId param when domain createdBy is blank`() {
            val contributionBlankCreatedBy = fullContribution.copy(createdBy = "")

            val document = contributionBlankCreatedBy.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals(testActorId, document.createdBy)
        }

        @Test
        fun `maps null subunitId correctly`() {
            val contributionNoSubunit = fullContribution.copy(subunitId = null)

            val document = contributionNoSubunit.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertNull(document.subunitId)
        }

        @Test
        fun `maps linkedExpenseId correctly when present`() {
            val contributionWithLinkedExpense = fullContribution.copy(linkedExpenseId = testLinkedExpenseId)

            val document = contributionWithLinkedExpense.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals(testLinkedExpenseId, document.linkedExpenseId)
        }

        @Test
        fun `maps linkedSettlementId correctly when present`() {
            val contributionWithLinkedSettlement = fullContribution.copy(linkedSettlementId = "settlement-303")

            val document = contributionWithLinkedSettlement.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals("settlement-303", document.linkedSettlementId)
        }

        @Test
        fun `maps null linkedExpenseId correctly`() {
            val document = fullContribution.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertNull(document.linkedExpenseId)
            assertNull(document.linkedSettlementId)
        }

        @Test
        fun `maps createdAt and lastUpdatedAt when present`() {
            val document = fullContribution.toDocument(
                testContributionId,
                testGroupId,
                testActorId
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
                testActorId
            )

            // The mapper uses LocalDateTime.now() as fallback, so timestamps must be non-null
            assertNotNull(document.createdAt)
            assertNotNull(document.lastUpdatedAt)
        }

        @Test
        fun `maps contributionScope, equivalentBaseAmount, exchangeRate, and contributionDate correctly`() {
            val contribution = fullContribution.copy(
                contributionScope = PayerType.SUBUNIT,
                equivalentBaseAmount = 60000L,
                exchangeRate = BigDecimal("1.25"),
                contributionDate = testTimestamp
            )

            val document = contribution.toDocument(
                testContributionId,
                testGroupId,
                testActorId
            )

            assertEquals("SUBUNIT", document.contributionScope)
            assertEquals(60000L, document.equivalentBaseAmountCents)
            assertEquals("1.25", document.exchangeRate)
            assertEquals(testFirebaseTimestamp, document.contributionDate)
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
            createdBy = testActorId,
            createdAt = testFirebaseTimestamp,
            lastUpdatedAt = testFirebaseTimestamp
        )

        @Test
        fun `maps all core fields correctly`() {
            val contribution = fullDocument.toDomain()

            assertEquals(testContributionId, contribution.id)
            assertEquals(testGroupId, contribution.groupId)
            assertEquals(testUserId, contribution.userId)
            assertEquals(testActorId, contribution.createdBy)
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
        fun `maps linkedExpenseId correctly when present`() {
            val documentWithLinkedExpense = fullDocument.copy(linkedExpenseId = testLinkedExpenseId)

            val contribution = documentWithLinkedExpense.toDomain()

            assertEquals(testLinkedExpenseId, contribution.linkedExpenseId)
        }

        @Test
        fun `maps linkedSettlementId correctly when present`() {
            val documentWithLinkedSettlement = fullDocument.copy(linkedSettlementId = "settlement-303")

            val contribution = documentWithLinkedSettlement.toDomain()

            assertEquals("settlement-303", contribution.linkedSettlementId)
        }

        @Test
        fun `maps null linkedExpenseId correctly`() {
            val contribution = fullDocument.toDomain()

            assertNull(contribution.linkedExpenseId)
            assertNull(contribution.linkedSettlementId)
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

        @Test
        fun `infers SUBUNIT contribution scope when scope is blank and subunitId is present`() {
            val doc = fullDocument.copy(contributionScope = "", subunitId = testSubunitId)
            val domain = doc.toDomain()
            assertEquals(PayerType.SUBUNIT, domain.contributionScope)
        }

        @Test
        fun `infers USER contribution scope when scope is blank and subunitId is null`() {
            val doc = fullDocument.copy(contributionScope = "", subunitId = null)
            val domain = doc.toDomain()
            assertEquals(PayerType.USER, domain.contributionScope)
        }

        @Test
        fun `parses valid contribution scope string`() {
            val doc = fullDocument.copy(contributionScope = "GROUP")
            val domain = doc.toDomain()
            assertEquals(PayerType.GROUP, domain.contributionScope)
        }

        @Test
        fun `falls back to USER contribution scope when scope string is invalid`() {
            val doc = fullDocument.copy(contributionScope = "INVALID_SCOPE")
            val domain = doc.toDomain()
            assertEquals(PayerType.USER, domain.contributionScope)
        }

        @Test
        fun `maps exchangeRate parsing valid BigDecimal string`() {
            val doc = fullDocument.copy(exchangeRate = "1.3333")
            val domain = doc.toDomain()
            assertEquals(BigDecimal("1.3333"), domain.exchangeRate)
        }

        @Test
        fun `handles invalid exchangeRate string gracefully by returning null`() {
            val doc = fullDocument.copy(exchangeRate = "invalid-number")
            val domain = doc.toDomain()
            assertNull(domain.exchangeRate)
        }

        @Test
        fun `maps contributionDate when present`() {
            val doc = fullDocument.copy(contributionDate = testFirebaseTimestamp)
            val domain = doc.toDomain()
            assertEquals(testTimestamp, domain.contributionDate)
        }

        @Test
        fun `maps null contributionDate to null`() {
            val doc = fullDocument.copy(contributionDate = null)
            val domain = doc.toDomain()
            assertNull(domain.contributionDate)
        }
    }
}
