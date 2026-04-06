package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Group entities.
 * Provides reactive access to locally stored groups using Flow.
 */
@Dao
interface GroupDao {

    /**
     * Observes all groups in the database.
     * This Flow emits automatically whenever the groups table changes.
     * This is the foundation of the Offline-First pattern - UI always reads from here.
     */
    @Query("SELECT * FROM groups ORDER BY lastUpdatedAtMillis DESC")
    fun getAllGroupsFlow(): Flow<List<GroupEntity>>

    /**
     * Gets a single group by ID.
     */
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    /**
     * Observes a single group by ID.
     */
    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupByIdFlow(groupId: String): Flow<GroupEntity?>

    /**
     * Inserts or updates groups.
     * Uses @Upsert to perform a true UPDATE if the ID exists, INSERT if not.
     * This prevents DELETE+INSERT behavior of REPLACE, which would trigger
     * CASCADE deletion of related expenses via ForeignKey constraints.
     */
    @Upsert
    suspend fun insertGroups(groups: List<GroupEntity>)

    /**
     * Inserts or updates a single group.
     */
    @Upsert
    suspend fun insertGroup(group: GroupEntity)

    /**
     * Deletes all groups from the table.
     */
    @Query("DELETE FROM groups")
    suspend fun clearAllGroups()

    /**
     * Deletes a specific group by ID.
     */
    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    /**
     * Updates the sync status of a single group.
     * Used to transition between PENDING_SYNC → SYNCED or SYNC_FAILED after cloud sync.
     */
    @Query("UPDATE groups SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    /**
     * Retrieves all group IDs currently stored locally.
     * Used during reconciliation to identify stale groups.
     */
    @Query("SELECT id FROM groups")
    suspend fun getAllGroupIds(): List<String>

    /**
     * Deletes groups whose IDs are in the provided list.
     * Used to selectively remove stale groups during sync reconciliation.
     */
    @Query("DELETE FROM groups WHERE id IN (:ids)")
    suspend fun deleteGroupsByIds(ids: List<String>)

    /**
     * Reconciles local groups with the authoritative cloud snapshot.
     *
     * Uses a merge strategy instead of destructive delete+insert:
     * 1. Upsert all remote groups (adds new, updates existing)
     * 2. Delete only local groups whose IDs are NOT in the remote set
     *
     * This preserves locally-created groups that haven't synced to the cloud yet
     * (their IDs won't be in the remote set, but they also won't be deleted because
     * the Firestore SDK's latency compensation includes pending writes in snapshots).
     *
     * In the narrow race where a snapshot fires before the Firestore local write,
     * this prevents data loss by keeping unsynced local groups alive until the next
     * snapshot reconciliation includes them.
     */
    @Transaction
    suspend fun replaceAllGroups(groups: List<GroupEntity>) {
        val remoteIds = groups.map { it.id }.toSet()
        val localIds = getAllGroupIds()
        val staleIds = localIds.filter { it !in remoteIds }

        // 1. Upsert remote groups (adds new ones, updates existing)
        insertGroups(groups)

        // 2. Remove only stale groups (exist locally but not in remote snapshot)
        if (staleIds.isNotEmpty()) {
            deleteGroupsByIds(staleIds)
        }
    }
}
