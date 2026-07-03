package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.SettlementRecordEntity
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettlementRecordEntityMapperTest {

    private val testId = "settlement-1"
    private val testGroupId = "group-123"
    private val testFromUserId = "user-1"
    private val testToUserId = "user-2"
    private val testAmount = 1000L
    private val testCurrency = "EUR"
    private val testSourcePocket = SettlementPocketType.CASH
    private val testStatus = SettlementStatus.SUGGESTED
    private val testTimestamp = LocalDateTime.of(2026, 7, 2, 12, 0, 0)
    private val testTimestampMillis = testTimestamp
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

    private val fullDomain = SettlementRecord(
        id = testId,
        groupId = testGroupId,
        settlement = Settlement(
            fromUserId = testFromUserId,
            toUserId = testToUserId,
            amount = testAmount,
            currency = testCurrency,
            sourcePocket = testSourcePocket
        ),
        status = testStatus,
        createdAt = testTimestamp,
        confirmedByPayerAt = testTimestamp,
        confirmedByPayeeAt = testTimestamp,
        resolvedAt = testTimestamp,
        disputedBy = "user-3",
        disputeReason = "Wrong amount"
    )

    @Nested
    inner class ToDomain {

        @Test
        fun `maps all fields correctly`() {
            val entity = SettlementRecordEntity(
                id = testId,
                groupId = testGroupId,
                fromUserId = testFromUserId,
                toUserId = testToUserId,
                amountCents = testAmount,
                currency = testCurrency,
                sourcePocket = testSourcePocket.name,
                status = testStatus.name,
                syncStatus = "SYNCED",
                createdAt = testTimestampMillis,
                confirmedByPayerAt = testTimestampMillis,
                confirmedByPayeeAt = testTimestampMillis,
                resolvedAt = testTimestampMillis,
                disputedBy = "user-3",
                disputeReason = "Wrong amount"
            )

            val result = entity.toDomain()

            assertEquals(testId, result.id)
            assertEquals(testGroupId, result.groupId)
            assertEquals(testFromUserId, result.settlement.fromUserId)
            assertEquals(testToUserId, result.settlement.toUserId)
            assertEquals(testAmount, result.settlement.amount)
            assertEquals(testCurrency, result.settlement.currency)
            assertEquals(testSourcePocket, result.settlement.sourcePocket)
            assertEquals(testStatus, result.status)
            assertEquals(testTimestamp, result.createdAt)
            assertEquals(testTimestamp, result.confirmedByPayerAt)
            assertEquals(testTimestamp, result.confirmedByPayeeAt)
            assertEquals(testTimestamp, result.resolvedAt)
            assertEquals("user-3", result.disputedBy)
            assertEquals("Wrong amount", result.disputeReason)
        }

        @Test
        fun `nullable timestamps map to null`() {
            val entity = SettlementRecordEntity(
                id = testId, groupId = testGroupId,
                fromUserId = testFromUserId, toUserId = testToUserId,
                amountCents = testAmount, currency = testCurrency,
                sourcePocket = testSourcePocket.name,
                status = testStatus.name, syncStatus = "SYNCED",
                createdAt = testTimestampMillis,
                confirmedByPayerAt = null, confirmedByPayeeAt = null,
                resolvedAt = null
            )

            val result = entity.toDomain()

            assertNull(result.confirmedByPayerAt)
            assertNull(result.confirmedByPayeeAt)
            assertNull(result.resolvedAt)
        }
    }

    @Nested
    inner class ToEntity {

        @Test
        fun `maps all fields correctly`() {
            val entity = fullDomain.toEntity()

            assertEquals(testId, entity.id)
            assertEquals(testGroupId, entity.groupId)
            assertEquals(testFromUserId, entity.fromUserId)
            assertEquals(testToUserId, entity.toUserId)
            assertEquals(testAmount, entity.amountCents)
            assertEquals(testCurrency, entity.currency)
            assertEquals(testSourcePocket.name, entity.sourcePocket)
            assertEquals(testStatus.name, entity.status)
            assertEquals("SYNCED", entity.syncStatus)
            assertEquals(testTimestampMillis, entity.createdAt)
            assertEquals(testTimestampMillis, entity.confirmedByPayerAt)
            assertEquals(testTimestampMillis, entity.confirmedByPayeeAt)
            assertEquals(testTimestampMillis, entity.resolvedAt)
            assertEquals("user-3", entity.disputedBy)
            assertEquals("Wrong amount", entity.disputeReason)
        }

        @Test
        fun `nullable timestamps map to null`() {
            val domain = fullDomain.copy(
                confirmedByPayerAt = null,
                confirmedByPayeeAt = null,
                resolvedAt = null
            )

            val entity = domain.toEntity()

            assertNull(entity.confirmedByPayerAt)
            assertNull(entity.confirmedByPayeeAt)
            assertNull(entity.resolvedAt)
        }
    }
}
