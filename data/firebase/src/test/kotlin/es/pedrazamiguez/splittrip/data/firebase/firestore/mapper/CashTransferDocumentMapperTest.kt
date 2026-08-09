package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import es.pedrazamiguez.splittrip.data.firebase.firestore.document.CashTransferDocument
import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CashTransferDocumentMapperTest {

    @Test
    fun `toDocument maps domain to document correctly`() {
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

        val document = domain.toDocument()

        assertEquals("group-123", document.groupId)
        assertEquals("user-1", document.fromUserId)
        assertEquals("user-2", document.toUserId)
        assertEquals(5000L, document.amountCents)
        assertEquals("USD", document.currency)
        assertEquals(5000L, document.equivalentBaseAmountCents)
        assertEquals(123456789L, document.createdAt)
    }

    @Test
    fun `toDomain maps document to domain correctly`() {
        val document = CashTransferDocument(
            groupId = "group-123",
            fromUserId = "user-1",
            toUserId = "user-2",
            amountCents = 5000L,
            currency = "USD",
            equivalentBaseAmountCents = 5000L,
            createdAt = 123456789L
        )

        val domain = document.toDomain("transfer-123")

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
