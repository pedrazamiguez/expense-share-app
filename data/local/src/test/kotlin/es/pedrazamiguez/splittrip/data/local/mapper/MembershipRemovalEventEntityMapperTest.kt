package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.data.local.entity.MembershipRemovalEventEntity
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.MembershipRemovalEvent
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MembershipRemovalEventEntityMapperTest {

    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val testTimestampMillis = testTimestamp.toEpochMillisUtc()

    private val entity = MembershipRemovalEventEntity(
        id = "event-123",
        groupId = "group-123",
        userId = "user-123",
        createdAtMillis = testTimestampMillis,
        processed = false,
        syncStatus = "PENDING_SYNC"
    )

    private val domain = MembershipRemovalEvent(
        id = "event-123",
        groupId = "group-123",
        userId = "user-123",
        createdAt = testTimestamp,
        processed = false,
        syncStatus = SyncStatus.PENDING_SYNC
    )

    @Test
    fun `toDomain maps all fields correctly`() {
        val mapped = entity.toDomain()
        assertEquals(entity.id, mapped.id)
        assertEquals(entity.groupId, mapped.groupId)
        assertEquals(entity.userId, mapped.userId)
        assertEquals(testTimestamp, mapped.createdAt)
        assertEquals(entity.processed, mapped.processed)
        assertEquals(SyncStatus.PENDING_SYNC, mapped.syncStatus)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val mapped = domain.toEntity()
        assertEquals(domain.id, mapped.id)
        assertEquals(domain.groupId, mapped.groupId)
        assertEquals(domain.userId, mapped.userId)
        assertEquals(testTimestampMillis, mapped.createdAtMillis)
        assertEquals(domain.processed, mapped.processed)
        assertEquals("PENDING_SYNC", mapped.syncStatus)
    }

    @Test
    fun `list toDomain maps correctly`() {
        val list = listOf(entity)
        val mapped = list.toDomain()
        assertEquals(1, mapped.size)
        assertEquals(entity.id, mapped[0].id)
    }

    @Test
    fun `list toEntity maps correctly`() {
        val list = listOf(domain)
        val mapped = list.toEntity()
        assertEquals(1, mapped.size)
        assertEquals(domain.id, mapped[0].id)
    }

    @Test
    fun `MembershipRemovalEventEntity content equality and helpers`() {
        val entity1 = entity.copy()
        val entity2 = entity.copy(id = "other")

        assertEquals(entity1, entity)
        assertEquals(entity1.hashCode(), entity.hashCode())
        assertTrue(entity1.toString().contains("event-123"))

        assertFalse(entity1 == entity2)
    }
}
