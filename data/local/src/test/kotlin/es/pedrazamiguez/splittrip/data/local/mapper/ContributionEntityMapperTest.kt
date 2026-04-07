package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.ContributionEntity
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Contribution
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContributionEntityMapperTest {

    private val testContributionId = "contrib-123"
    private val testGroupId = "group-456"
    private val testUserId = "user-789"
    private val testCreatedBy = "actor-111"
    private val testSubunitId = "subunit-101"
    private val testLinkedExpenseId = "expense-202"
    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val testTimestampMillis = testTimestamp
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

    @Nested
    inner class ToDomain {

        private val fullEntity = ContributionEntity(
            id = testContributionId,
            groupId = testGroupId,
            userId = testUserId,
            createdBy = testCreatedBy,
            subunitId = testSubunitId,
            amount = 50000L,
            currency = "EUR",
            createdAtMillis = testTimestampMillis,
            lastUpdatedAtMillis = testTimestampMillis
        )

        @Test
        fun `maps all core fields correctly`() {
            val contribution = fullEntity.toDomain()

            assertEquals(testContributionId, contribution.id)
            assertEquals(testGroupId, contribution.groupId)
            assertEquals(testUserId, contribution.userId)
            assertEquals(testCreatedBy, contribution.createdBy)
            assertEquals(testSubunitId, contribution.subunitId)
            assertEquals(50000L, contribution.amount)
            assertEquals("EUR", contribution.currency)
        }

        @Test
        fun `maps null subunitId correctly`() {
            val entityNoSubunit = fullEntity.copy(subunitId = null)

            val contribution = entityNoSubunit.toDomain()

            assertNull(contribution.subunitId)
        }

        @Test
        fun `maps linkedExpenseId correctly when present`() {
            val entityWithLinkedExpense = fullEntity.copy(linkedExpenseId = testLinkedExpenseId)

            val contribution = entityWithLinkedExpense.toDomain()

            assertEquals(testLinkedExpenseId, contribution.linkedExpenseId)
        }

        @Test
        fun `maps null linkedExpenseId correctly`() {
            val contribution = fullEntity.toDomain()

            assertNull(contribution.linkedExpenseId)
        }

        @Test
        fun `maps timestamps correctly`() {
            val contribution = fullEntity.toDomain()

            assertEquals(testTimestamp, contribution.createdAt)
            assertEquals(testTimestamp, contribution.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null domain fields`() {
            val entityNullTimestamps = fullEntity.copy(
                createdAtMillis = null,
                lastUpdatedAtMillis = null
            )

            val contribution = entityNullTimestamps.toDomain()

            assertNull(contribution.createdAt)
            assertNull(contribution.lastUpdatedAt)
        }

        @Test
        fun `list toDomain maps all items`() {
            val entities = listOf(
                fullEntity,
                fullEntity.copy(id = "contrib-456", subunitId = null, amount = 30000L)
            )

            val contributions = entities.toDomain()

            assertEquals(2, contributions.size)
            assertEquals("contrib-123", contributions[0].id)
            assertEquals(testSubunitId, contributions[0].subunitId)
            assertEquals("contrib-456", contributions[1].id)
            assertNull(contributions[1].subunitId)
        }

        @Test
        fun `default createdBy maps to empty string`() {
            val entityNoCreatedBy = fullEntity.copy(createdBy = "")

            val contribution = entityNoCreatedBy.toDomain()

            assertEquals("", contribution.createdBy)
        }

        @Test
        fun `default syncStatus maps to SYNCED`() {
            val contribution = fullEntity.toDomain()

            assertEquals(SyncStatus.SYNCED, contribution.syncStatus)
        }

        @Test
        fun `PENDING_SYNC syncStatus maps correctly`() {
            val entity = fullEntity.copy(syncStatus = "PENDING_SYNC")

            val contribution = entity.toDomain()

            assertEquals(SyncStatus.PENDING_SYNC, contribution.syncStatus)
        }

        @Test
        fun `SYNC_FAILED syncStatus maps correctly`() {
            val entity = fullEntity.copy(syncStatus = "SYNC_FAILED")

            val contribution = entity.toDomain()

            assertEquals(SyncStatus.SYNC_FAILED, contribution.syncStatus)
        }
    }

    @Nested
    inner class ToEntity {

        private val fullContribution = Contribution(
            id = testContributionId,
            groupId = testGroupId,
            userId = testUserId,
            createdBy = testCreatedBy,
            subunitId = testSubunitId,
            amount = 50000L,
            currency = "EUR",
            createdAt = testTimestamp,
            lastUpdatedAt = testTimestamp
        )

        @Test
        fun `maps all core fields correctly`() {
            val entity = fullContribution.toEntity()

            assertEquals(testContributionId, entity.id)
            assertEquals(testGroupId, entity.groupId)
            assertEquals(testUserId, entity.userId)
            assertEquals(testCreatedBy, entity.createdBy)
            assertEquals(testSubunitId, entity.subunitId)
            assertEquals(50000L, entity.amount)
            assertEquals("EUR", entity.currency)
        }

        @Test
        fun `maps null subunitId correctly`() {
            val contributionNoSubunit = fullContribution.copy(subunitId = null)

            val entity = contributionNoSubunit.toEntity()

            assertNull(entity.subunitId)
        }

        @Test
        fun `maps linkedExpenseId correctly when present`() {
            val contributionWithLinkedExpense = fullContribution.copy(linkedExpenseId = testLinkedExpenseId)

            val entity = contributionWithLinkedExpense.toEntity()

            assertEquals(testLinkedExpenseId, entity.linkedExpenseId)
        }

        @Test
        fun `maps null linkedExpenseId correctly`() {
            val entity = fullContribution.toEntity()

            assertNull(entity.linkedExpenseId)
        }

        @Test
        fun `maps timestamps correctly`() {
            val entity = fullContribution.toEntity()

            assertEquals(testTimestampMillis, entity.createdAtMillis)
            assertEquals(testTimestampMillis, entity.lastUpdatedAtMillis)
        }

        @Test
        fun `null timestamps generate current time defaults`() {
            val contributionNullTimestamps = fullContribution.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val entity = contributionNullTimestamps.toEntity()

            assertNotNull(entity.createdAtMillis)
            assertNotNull(entity.lastUpdatedAtMillis)
            assertTrue(entity.createdAtMillis!! > 0)
            assertEquals(entity.createdAtMillis, entity.lastUpdatedAtMillis)
        }

        @Test
        fun `list toEntity maps all items`() {
            val contributions = listOf(
                fullContribution,
                fullContribution.copy(id = "contrib-456", subunitId = null, amount = 30000L)
            )

            val entities = contributions.toEntity()

            assertEquals(2, entities.size)
            assertEquals("contrib-123", entities[0].id)
            assertEquals(testSubunitId, entities[0].subunitId)
            assertEquals("contrib-456", entities[1].id)
            assertNull(entities[1].subunitId)
        }

        @Test
        fun `maps syncStatus to entity string`() {
            val pending = fullContribution.copy(syncStatus = SyncStatus.PENDING_SYNC)

            val entity = pending.toEntity()

            assertEquals("PENDING_SYNC", entity.syncStatus)
        }
    }
}
