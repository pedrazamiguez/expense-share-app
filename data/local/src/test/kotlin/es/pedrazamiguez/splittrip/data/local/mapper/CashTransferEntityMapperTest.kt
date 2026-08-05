package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.CashTransferEntity
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CashTransferEntityMapperTest {

    @Test
    fun `toEntity maps domain to entity correctly`() {
        val domain = CashTransfer(
            id = "transfer-123",
            groupId = "group-123",
            fromUserId = "user-1",
            toUserId = "user-2",
            amountCents = 5000L,
            currency = "USD",
            equivalentBaseAmountCents = 5000L,
            createdAt = 123456789L
        )

        val entity = domain.toEntity(SyncStatus.PENDING_SYNC)

        assertEquals("transfer-123", entity.id)
        assertEquals("group-123", entity.groupId)
        assertEquals("user-1", entity.fromUserId)
        assertEquals("user-2", entity.toUserId)
        assertEquals(5000L, entity.amountCents)
        assertEquals("USD", entity.currency)
        assertEquals(5000L, entity.equivalentBaseAmountCents)
        assertEquals(123456789L, entity.createdAt)
        assertEquals(SyncStatus.PENDING_SYNC, entity.syncStatus)
    }

    @Test
    fun `toDomain maps entity to domain correctly`() {
        val entity = CashTransferEntity(
            id = "transfer-123",
            groupId = "group-123",
            fromUserId = "user-1",
            toUserId = "user-2",
            amountCents = 5000L,
            currency = "USD",
            equivalentBaseAmountCents = 5000L,
            createdAt = 123456789L,
            syncStatus = SyncStatus.SYNCED
        )

        val domain = entity.toDomain()

        assertEquals("transfer-123", domain.id)
        assertEquals("group-123", domain.groupId)
        assertEquals("user-1", domain.fromUserId)
        assertEquals("user-2", domain.toUserId)
        assertEquals(5000L, domain.amountCents)
        assertEquals("USD", domain.currency)
        assertEquals(5000L, domain.equivalentBaseAmountCents)
        assertEquals(123456789L, domain.createdAt)
    }
}
