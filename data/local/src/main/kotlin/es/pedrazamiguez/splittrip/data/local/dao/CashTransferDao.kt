package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import es.pedrazamiguez.splittrip.data.local.entity.CashTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashTransferDao {
    @Query("SELECT * FROM cash_transfers WHERE groupId = :groupId")
    fun observeByGroupId(groupId: String): Flow<List<CashTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: CashTransferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transfers: List<CashTransferEntity>)

    @Query("DELETE FROM cash_transfers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cash_transfers WHERE groupId = :groupId AND id NOT IN (:retainedIds)")
    suspend fun deleteStaleForGroup(groupId: String, retainedIds: List<String>)

    @Query("UPDATE cash_transfers SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("SELECT id FROM cash_transfers WHERE groupId = :groupId AND syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSyncCashTransferIds(groupId: String): List<String>

    @Query("SELECT * FROM cash_transfers WHERE id = :id")
    suspend fun getCashTransferById(id: String): CashTransferEntity?

    @Query("DELETE FROM cash_transfers WHERE groupId = :groupId")
    suspend fun deleteByGroupId(groupId: String)

    @Transaction
    suspend fun replaceCashTransfersForGroup(groupId: String, remoteTransfers: List<CashTransferEntity>) {
        insertAll(remoteTransfers)
        val retainedIds = remoteTransfers.map { it.id }
        if (retainedIds.isNotEmpty()) {
            deleteStaleForGroup(groupId, retainedIds)
        } else {
            deleteByGroupId(groupId)
        }
    }
}
