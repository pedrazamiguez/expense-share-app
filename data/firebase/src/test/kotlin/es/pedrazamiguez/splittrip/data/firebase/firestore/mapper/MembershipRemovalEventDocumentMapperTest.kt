package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.MembershipRemovalEvent
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MembershipRemovalEventDocumentMapperTest {

    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val expectedFirebaseTimestamp = testTimestamp.toTimestampUtc()!!

    private val domainEvent = MembershipRemovalEvent(
        id = "event-123",
        groupId = "group-123",
        userId = "user-123",
        createdAt = testTimestamp,
        processed = false,
        syncStatus = SyncStatus.PENDING_SYNC
    )

    @Test
    fun `toDocument maps fields correctly`() {
        val document = domainEvent.toDocument(eventId = "event-123", groupId = "group-123")

        assertEquals("event-123", document.eventId)
        assertEquals("group-123", document.groupId)
        assertEquals(domainEvent.userId, document.userId)
        assertEquals(expectedFirebaseTimestamp, document.createdAt)
    }
}
