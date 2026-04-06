package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.CashWithdrawalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashWithdrawalDao {

    @Upsert
    suspend fun insertWithdrawal(withdrawal: CashWithdrawalEntity)

    @Upsert
    suspend fun insertWithdrawals(withdrawals: List<CashWithdrawalEntity>)

    @Query("SELECT * FROM cash_withdrawals WHERE groupId = :groupId ORDER BY createdAtMillis DESC")
    fun getWithdrawalsByGroupIdFlow(groupId: String): Flow<List<CashWithdrawalEntity>>

    @Query("SELECT * FROM cash_withdrawals WHERE id = :withdrawalId")
    suspend fun getWithdrawalById(withdrawalId: String): CashWithdrawalEntity?

    /**
     * Fetches available (non-exhausted) withdrawals for FIFO consumption.
     * Ordered by createdAt ascending (oldest first) for FIFO ordering.
     */
    @Query(
        """
        SELECT * FROM cash_withdrawals
        WHERE groupId = :groupId AND currency = :currency AND remainingAmount > 0
        ORDER BY createdAtMillis ASC
        """
    )
    suspend fun getAvailableByGroupAndCurrency(groupId: String, currency: String): List<CashWithdrawalEntity>

    @Query("UPDATE cash_withdrawals SET remainingAmount = :newRemaining WHERE id = :withdrawalId")
    suspend fun updateRemainingAmount(withdrawalId: String, newRemaining: Long)

    /**
     * Atomically updates the remaining amount on multiple withdrawals in a single transaction.
     * Used during FIFO cash expense processing to batch all tranche deductions together.
     */
    @Transaction
    suspend fun updateRemainingAmounts(updates: List<Pair<String, Long>>) {
        for ((withdrawalId, newRemaining) in updates) {
            updateRemainingAmount(withdrawalId, newRemaining)
        }
    }

    @Query("DELETE FROM cash_withdrawals WHERE id = :withdrawalId")
    suspend fun deleteWithdrawal(withdrawalId: String)

    /**
     * Updates the sync status of a single cash withdrawal.
     * Used to transition between PENDING_SYNC → SYNCED or SYNC_FAILED after cloud sync.
     */
    @Query("UPDATE cash_withdrawals SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("DELETE FROM cash_withdrawals WHERE groupId = :groupId")
    suspend fun deleteWithdrawalsByGroupId(groupId: String)

    @Query("SELECT id FROM cash_withdrawals WHERE groupId = :groupId")
    suspend fun getWithdrawalIdsByGroupId(groupId: String): List<String>

    @Query("DELETE FROM cash_withdrawals")
    suspend fun clearAllWithdrawals()

    /**
     * Deletes withdrawals whose IDs are in the provided list.
     * Used to selectively remove stale withdrawals during sync reconciliation.
     */
    @Query("DELETE FROM cash_withdrawals WHERE id IN (:ids)")
    suspend fun deleteWithdrawalsByIds(ids: List<String>)

    /**
     * Reconciles local withdrawals for a group with the authoritative cloud snapshot.
     *
     * Uses a merge strategy instead of destructive delete+insert:
     * 1. Upsert all remote withdrawals (adds new, updates existing)
     * 2. Delete only local withdrawals whose IDs are NOT in the remote set
     *
     * This preserves locally-created withdrawals that haven't synced to the cloud yet.
     */
    @Transaction
    suspend fun replaceWithdrawalsForGroup(groupId: String, withdrawals: List<CashWithdrawalEntity>) {
        val remoteIds = withdrawals.map { it.id }.toSet()
        val localIds = getWithdrawalIdsByGroupId(groupId)
        val staleIds = localIds.filter { it !in remoteIds }

        // 1. Upsert remote withdrawals (adds new ones, updates existing)
        insertWithdrawals(withdrawals)

        // 2. Remove only stale withdrawals (exist locally but not in remote snapshot)
        if (staleIds.isNotEmpty()) {
            deleteWithdrawalsByIds(staleIds)
        }
    }
}
