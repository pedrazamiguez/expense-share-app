package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.SubunitEntity
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
     * Deletes subunits whose IDs are in the provided list.
     * Used to selectively remove stale subunits during sync reconciliation.
     */
    @Query("DELETE FROM subunits WHERE id IN (:ids)")
    suspend fun deleteSubunitsByIds(ids: List<String>)

    /**
     * Reconciles local subunits for a group with the authoritative cloud snapshot.
     *
     * Uses a merge strategy instead of destructive delete+insert:
     * 1. Upsert all remote subunits (adds new, updates existing)
     * 2. Delete only local subunits whose IDs are NOT in the remote set
     *
     * This preserves locally-created subunits that haven't synced to the cloud yet.
     */
    @Transaction
    suspend fun replaceSubunitsForGroup(groupId: String, subunits: List<SubunitEntity>) {
        val remoteIds = subunits.map { it.id }.toSet()
        val localIds = getSubunitIdsByGroupId(groupId)
        val staleIds = localIds.filter { it !in remoteIds }

        // 1. Upsert remote subunits (adds new ones, updates existing)
        insertSubunits(subunits)

        // 2. Remove only stale subunits (exist locally but not in remote snapshot)
        if (staleIds.isNotEmpty()) {
            deleteSubunitsByIds(staleIds)
        }
    }
}
