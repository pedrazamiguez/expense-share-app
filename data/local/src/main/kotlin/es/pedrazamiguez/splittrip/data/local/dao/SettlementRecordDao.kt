package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.SettlementRecordEntity
import es.pedrazamiguez.splittrip.data.local.entity.SyncStatusEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementRecordDao {

    @Query("SELECT * FROM settlement_records WHERE groupId = :groupId")
    fun getByGroupIdFlow(groupId: String): Flow<List<SettlementRecordEntity>>

    @Query("SELECT * FROM settlement_records WHERE groupId = :groupId")
    suspend fun getByGroupId(groupId: String): List<SettlementRecordEntity>

    @Query(
        "SELECT * FROM settlement_records WHERE groupId = :groupId " +
            "AND (fromUserId = :userId OR toUserId = :userId)"
    )
    suspend fun getByMember(groupId: String, userId: String): List<SettlementRecordEntity>

    @Query("SELECT * FROM settlement_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SettlementRecordEntity?

    @Upsert
    suspend fun upsert(entity: SettlementRecordEntity)

    @Query("DELETE FROM settlement_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE settlement_records SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String)

    @Query("SELECT syncStatus FROM settlement_records WHERE id = :id LIMIT 1")
    suspend fun getSyncStatus(id: String): String?

    @Query(
        "SELECT id FROM settlement_records WHERE groupId = :groupId " +
            "AND syncStatus = 'PENDING_SYNC'"
    )
    suspend fun getPendingSyncIds(groupId: String): List<String>

    @Query("DELETE FROM settlement_records WHERE groupId = :groupId")
    suspend fun deleteByGroupId(groupId: String)

    @Query("SELECT id, syncStatus FROM settlement_records WHERE groupId = :groupId AND syncStatus != 'SYNCED'")
    suspend fun getUnsyncedSettlementStatuses(groupId: String): List<SyncStatusEntry>

    @Query("SELECT id FROM settlement_records WHERE groupId = :groupId")
    suspend fun getSettlementIdsByGroupId(groupId: String): List<String>

    @Query("DELETE FROM settlement_records WHERE id IN (:ids)")
    suspend fun deleteSettlementsByIds(ids: List<String>)

    @Transaction
    suspend fun replaceForGroup(groupId: String, entities: List<SettlementRecordEntity>) {
        val unsyncedStatuses = getUnsyncedSettlementStatuses(groupId)
        val unsyncedIds = unsyncedStatuses.map { it.id }.toSet()

        val remoteIds = entities.map { it.id }.toSet()
        val localIds = getSettlementIdsByGroupId(groupId)

        entities.forEach { upsert(it) }

        for (entry in unsyncedStatuses) {
            updateSyncStatus(entry.id, entry.syncStatus)
        }

        val staleIds = localIds.filter { it !in remoteIds && it !in unsyncedIds }
        if (staleIds.isNotEmpty()) {
            deleteSettlementsByIds(staleIds)
        }
    }
}
