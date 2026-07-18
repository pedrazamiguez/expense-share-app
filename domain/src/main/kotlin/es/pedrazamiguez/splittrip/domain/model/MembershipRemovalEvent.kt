package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import java.time.LocalDateTime

/**
 * Traces a member departure event from a group.
 * Used for idempotency and sequential conflict resolution/re-splitting.
 *
 * @param id Unique identifier (UUID generated locally).
 * @param groupId The ID of the group the user left.
 * @param userId The ID of the user who left.
 * @param createdAt The timestamp of the departure.
 * @param processed Whether the client-side reactive re-splits have been computed.
 * @param syncStatus The synchronization state with the cloud database.
 */
data class MembershipRemovalEvent(
    val id: String,
    val groupId: String,
    val userId: String,
    val createdAt: LocalDateTime,
    val processed: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
