package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.CashWithdrawalEntity
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CashWithdrawalEntityMapperTest {

    private val testTimestamp = LocalDateTime.of(2026, 3, 15, 10, 30, 0)
    private val testTimestampMillis = testTimestamp
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

    private val fullEntity = CashWithdrawalEntity(
        id = "cw-1",
        groupId = "grp-1",
        withdrawnBy = "user-1",
        createdBy = "actor-1",
        withdrawalScope = "GROUP",
        subunitId = "sub-1",
        amountWithdrawn = 5000L,
        remainingAmount = 3000L,
        currency = "EUR",
        deductedBaseAmount = 4500L,
        exchangeRate = "0.9",
        createdAtMillis = testTimestampMillis,
        lastUpdatedAtMillis = testTimestampMillis,
        addOnsJson = null,
        title = "ATM withdrawal",
        notes = "For dinner",
        receiptLocalUri = "file://receipt.jpg"
    )

    @Nested
    inner class ToDomain {

        @Test
        fun `maps all core fields correctly`() {
            val cw = fullEntity.toDomain()

            assertEquals("cw-1", cw.id)
            assertEquals("grp-1", cw.groupId)
            assertEquals("user-1", cw.withdrawnBy)
            assertEquals("actor-1", cw.createdBy)
            assertEquals("sub-1", cw.subunitId)
            assertEquals(5000L, cw.amountWithdrawn)
            assertEquals(3000L, cw.remainingAmount)
            assertEquals("EUR", cw.currency)
            assertEquals(4500L, cw.deductedBaseAmount)
        }

        @Test
        fun `maps withdrawalScope enum correctly`() {
            val cw = fullEntity.toDomain()
            assertEquals(PayerType.GROUP, cw.withdrawalScope)
        }

        @Test
        fun `invalid withdrawalScope defaults to GROUP`() {
            val entity = fullEntity.copy(withdrawalScope = "INVALID")
            val cw = entity.toDomain()
            assertEquals(PayerType.GROUP, cw.withdrawalScope)
        }

        @Test
        fun `maps exchangeRate from String to BigDecimal`() {
            val cw = fullEntity.toDomain()
            assertEquals(0, BigDecimal("0.9").compareTo(cw.exchangeRate))
        }

        @Test
        fun `invalid exchangeRate defaults to ONE`() {
            val entity = fullEntity.copy(exchangeRate = "bad")
            val cw = entity.toDomain()
            assertEquals(0, BigDecimal.ONE.compareTo(cw.exchangeRate))
        }

        @Test
        fun `maps timestamps correctly`() {
            val cw = fullEntity.toDomain()
            assertEquals(testTimestamp, cw.createdAt)
            assertEquals(testTimestamp, cw.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null`() {
            val entity = fullEntity.copy(createdAtMillis = null, lastUpdatedAtMillis = null)
            val cw = entity.toDomain()
            assertNull(cw.createdAt)
            assertNull(cw.lastUpdatedAt)
        }

        @Test
        fun `null addOns defaults to empty list`() {
            val cw = fullEntity.toDomain()
            assertTrue(cw.addOns.isEmpty())
        }

        @Test
        fun `maps optional string fields`() {
            val cw = fullEntity.toDomain()
            assertEquals("ATM withdrawal", cw.title)
            assertEquals("For dinner", cw.notes)
            assertEquals("file://receipt.jpg", cw.receiptLocalUri)
        }

        @Test
        fun `default syncStatus maps to SYNCED`() {
            assertEquals(SyncStatus.SYNCED, fullEntity.toDomain().syncStatus)
        }

        @Test
        fun `PENDING_SYNC syncStatus maps correctly`() {
            val entity = fullEntity.copy(syncStatus = "PENDING_SYNC")
            assertEquals(SyncStatus.PENDING_SYNC, entity.toDomain().syncStatus)
        }

        @Test
        fun `SYNC_FAILED syncStatus maps correctly`() {
            val entity = fullEntity.copy(syncStatus = "SYNC_FAILED")
            assertEquals(SyncStatus.SYNC_FAILED, entity.toDomain().syncStatus)
        }
    }

    @Nested
    inner class ToEntity {

        private val fullDomain = CashWithdrawal(
            id = "cw-1",
            groupId = "grp-1",
            withdrawnBy = "user-1",
            createdBy = "actor-1",
            withdrawalScope = PayerType.SUBUNIT,
            subunitId = "sub-1",
            amountWithdrawn = 5000L,
            remainingAmount = 3000L,
            currency = "EUR",
            deductedBaseAmount = 4500L,
            exchangeRate = BigDecimal("0.9"),
            addOns = emptyList(),
            title = "ATM",
            notes = null,
            receiptLocalUri = null,
            createdAt = testTimestamp,
            lastUpdatedAt = testTimestamp
        )

        @Test
        fun `maps all core fields`() {
            val entity = fullDomain.toEntity()

            assertEquals("cw-1", entity.id)
            assertEquals("grp-1", entity.groupId)
            assertEquals("actor-1", entity.createdBy)
            assertEquals("SUBUNIT", entity.withdrawalScope)
            assertEquals("0.9", entity.exchangeRate)
        }

        @Test
        fun `null createdAt uses system time`() {
            val cw = fullDomain.copy(createdAt = null)
            val entity = cw.toEntity()
            assertNotNull(entity.createdAtMillis)
            assertTrue(entity.createdAtMillis!! > 0)
        }

        @Test
        fun `null lastUpdatedAt falls back to createdAt millis`() {
            val cw = fullDomain.copy(lastUpdatedAt = null)
            val entity = cw.toEntity()
            assertEquals(entity.createdAtMillis, entity.lastUpdatedAtMillis)
        }
    }

    @Nested
    inner class ListExtensions {

        @Test
        fun `toDomain maps list of entities`() {
            val entities = listOf(fullEntity, fullEntity.copy(id = "cw-2"))
            val results = entities.toDomain()
            assertEquals(2, results.size)
            assertEquals("cw-2", results[1].id)
        }

        @Test
        fun `toEntity maps list of domain objects`() {
            val cw = fullEntity.toDomain()
            val entities = listOf(cw, cw.copy(id = "cw-2")).toEntity()
            assertEquals(2, entities.size)
            assertEquals("cw-2", entities[1].id)
        }

        @Test
        fun `maps syncStatus to entity string`() {
            val cw = fullEntity.toDomain().copy(syncStatus = SyncStatus.PENDING_SYNC)
            assertEquals("PENDING_SYNC", cw.toEntity().syncStatus)
        }
    }
}
