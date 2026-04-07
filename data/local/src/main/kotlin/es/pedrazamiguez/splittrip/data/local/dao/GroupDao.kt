package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.GroupEntity
import es.pedrazamiguez.splittrip.data.local.entity.SyncStatusEntry
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
     * Returns sync status metadata for all groups that are NOT fully synced.
     * Used during cloud snapshot reconciliation to preserve PENDING_SYNC / SYNC_FAILED
     * statuses that would otherwise be overwritten by the upsert (which defaults to SYNCED).
     */
    @Query("SELECT id, syncStatus FROM groups WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedGroupStatuses(): List<SyncStatusEntry>

    /**
     * Returns IDs of groups that are still waiting for server confirmation.
     * Used after reconciliation to attempt server verification and transition to SYNCED.
     */
    @Query("SELECT id FROM groups WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSyncGroupIds(): List<String>

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
     * 1. Capture non-SYNCED statuses (PENDING_SYNC / SYNC_FAILED) before upsert
     * 2. Upsert all remote groups (adds new, updates existing — sets syncStatus to SYNCED)
     * 3. Restore non-SYNCED statuses that were captured in step 1
     * 4. Delete only stale synced groups (not in remote set AND fully synced)
     *
     * This preserves locally-created groups that haven't synced to the cloud yet:
     * - Their syncStatus (PENDING_SYNC / SYNC_FAILED) is restored after upsert
     * - They are protected from stale deletion even if not in the remote snapshot
     *
     * The Firestore SDK's latency compensation includes pending writes in snapshots,
     * so unsynced items typically appear in the remote set. The non-SYNCED protection
     * adds an extra safety net for the narrow race where a snapshot fires before
     * the Firestore SDK caches the pending write.
     */
    @Transaction
    suspend fun replaceAllGroups(groups: List<GroupEntity>) {
        // Step 1: Capture non-SYNCED statuses before the upsert overwrites them
        val unsyncedStatuses = getUnsyncedGroupStatuses()
        val unsyncedIds = unsyncedStatuses.map { it.id }.toSet()

        val remoteIds = groups.map { it.id }.toSet()
        val localIds = getAllGroupIds()

        // Step 2: Upsert remote groups (sets syncStatus to SYNCED for all)
        insertGroups(groups)

        // Step 3: Restore non-SYNCED statuses for items that existed before
        for (entry in unsyncedStatuses) {
            updateSyncStatus(entry.id, entry.syncStatus)
        }

        // Step 4: Remove stale groups — only those that are NOT in the remote set
        // AND are NOT in a non-SYNCED state (protect unsynced local data)
        val staleIds = localIds.filter { it !in remoteIds && it !in unsyncedIds }
        if (staleIds.isNotEmpty()) {
            deleteGroupsByIds(staleIds)
        }
    }
}
