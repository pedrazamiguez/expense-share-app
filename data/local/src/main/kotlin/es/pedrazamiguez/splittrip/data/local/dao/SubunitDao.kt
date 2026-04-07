package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.SubunitEntity
import es.pedrazamiguez.splittrip.data.local.entity.SyncStatusEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SubunitDao {

    @Upsert
    suspend fun insertSubunit(subunit: SubunitEntity)

    @Upsert
    suspend fun insertSubunits(subunits: List<SubunitEntity>)

    @Query("SELECT * FROM subunits WHERE groupId = :groupId ORDER BY name ASC")
    fun getSubunitsByGroupIdFlow(groupId: String): Flow<List<SubunitEntity>>

    @Query("SELECT * FROM subunits WHERE groupId = :groupId ORDER BY name ASC")
    suspend fun getSubunitsByGroupId(groupId: String): List<SubunitEntity>

    @Query("SELECT * FROM subunits WHERE id = :subunitId")
    suspend fun getSubunitById(subunitId: String): SubunitEntity?

    @Query("SELECT id FROM subunits WHERE groupId = :groupId")
    suspend fun getSubunitIdsByGroupId(groupId: String): List<String>

    @Query("DELETE FROM subunits WHERE id = :subunitId")
    suspend fun deleteSubunit(subunitId: String)

    /**
     * Updates the sync status of a single subunit.
     * Used to transition between PENDING_SYNC → SYNCED or SYNC_FAILED after cloud sync.
     */
    @Query("UPDATE subunits SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("DELETE FROM subunits WHERE groupId = :groupId")
    suspend fun deleteSubunitsByGroupId(groupId: String)

    @Query("DELETE FROM subunits")
    suspend fun clearAllSubunits()

    /**
     * Returns sync status metadata for subunits in a group that are NOT fully synced.
     * Used during cloud snapshot reconciliation to preserve PENDING_SYNC / SYNC_FAILED
     * statuses that would otherwise be overwritten by the upsert (which defaults to SYNCED).
     */
    @Query("SELECT id, syncStatus FROM subunits WHERE groupId = :groupId AND syncStatus != 'SYNCED'")
    suspend fun getUnsyncedSubunitStatuses(groupId: String): List<SyncStatusEntry>

    /**
     * Deletes subunits whose IDs are in the provided list.
     * Used to selectively remove stale subunits during sync reconciliation.
     */
    @Query("DELETE FROM subunits WHERE id IN (:ids)")
    suspend fun deleteSubunitsByIds(ids: List<String>)

    /**
     * Reconciles local subunits for a group with the authoritative cloud snapshot.
     *
     * Uses a merge strategy instead of destructive delete+insert:
     * 1. Capture non-SYNCED statuses (PENDING_SYNC / SYNC_FAILED) before upsert
     * 2. Upsert all remote subunits (adds new, updates existing — sets syncStatus to SYNCED)
     * 3. Restore non-SYNCED statuses that were captured in step 1
     * 4. Delete only stale synced subunits (not in remote set AND fully synced)
     *
     * This preserves locally-created subunits that haven't synced to the cloud yet.
     */
    @Transaction
    suspend fun replaceSubunitsForGroup(groupId: String, subunits: List<SubunitEntity>) {
        // Step 1: Capture non-SYNCED statuses before the upsert overwrites them
        val unsyncedStatuses = getUnsyncedSubunitStatuses(groupId)
        val unsyncedIds = unsyncedStatuses.map { it.id }.toSet()

        val remoteIds = subunits.map { it.id }.toSet()
        val localIds = getSubunitIdsByGroupId(groupId)

        // Step 2: Upsert remote subunits (sets syncStatus to SYNCED for all)
        insertSubunits(subunits)

        // Step 3: Restore non-SYNCED statuses for items that existed before
        for (entry in unsyncedStatuses) {
            updateSyncStatus(entry.id, entry.syncStatus)
        }

        // Step 4: Remove stale subunits — only those that are NOT in the remote set
        // AND are NOT in a non-SYNCED state (protect unsynced local data)
        val staleIds = localIds.filter { it !in remoteIds && it !in unsyncedIds }
        if (staleIds.isNotEmpty()) {
            deleteSubunitsByIds(staleIds)
        }
    }
}
