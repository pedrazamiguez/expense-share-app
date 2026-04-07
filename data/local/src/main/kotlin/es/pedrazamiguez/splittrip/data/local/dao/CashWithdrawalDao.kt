package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.splittrip.data.local.entity.CashWithdrawalEntity
import es.pedrazamiguez.splittrip.data.local.entity.SyncStatusEntry
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
     * Returns sync status metadata for withdrawals in a group that are NOT fully synced.
     * Used during cloud snapshot reconciliation to preserve PENDING_SYNC / SYNC_FAILED
     * statuses that would otherwise be overwritten by the upsert (which defaults to SYNCED).
     */
    @Query("SELECT id, syncStatus FROM cash_withdrawals WHERE groupId = :groupId AND syncStatus != 'SYNCED'")
    suspend fun getUnsyncedWithdrawalStatuses(groupId: String): List<SyncStatusEntry>

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
     * 1. Capture non-SYNCED statuses (PENDING_SYNC / SYNC_FAILED) before upsert
     * 2. Upsert all remote withdrawals (adds new, updates existing — sets syncStatus to SYNCED)
     * 3. Restore non-SYNCED statuses that were captured in step 1
     * 4. Delete only stale synced withdrawals (not in remote set AND fully synced)
     *
     * This preserves locally-created withdrawals that haven't synced to the cloud yet.
     */
    @Transaction
    suspend fun replaceWithdrawalsForGroup(groupId: String, withdrawals: List<CashWithdrawalEntity>) {
        // Step 1: Capture non-SYNCED statuses before the upsert overwrites them
        val unsyncedStatuses = getUnsyncedWithdrawalStatuses(groupId)
        val unsyncedIds = unsyncedStatuses.map { it.id }.toSet()

        val remoteIds = withdrawals.map { it.id }.toSet()
        val localIds = getWithdrawalIdsByGroupId(groupId)

        // Step 2: Upsert remote withdrawals (sets syncStatus to SYNCED for all)
        insertWithdrawals(withdrawals)

        // Step 3: Restore non-SYNCED statuses for items that existed before
        for (entry in unsyncedStatuses) {
            updateSyncStatus(entry.id, entry.syncStatus)
        }

        // Step 4: Remove stale withdrawals — only those that are NOT in the remote set
        // AND are NOT in a non-SYNCED state (protect unsynced local data)
        val staleIds = localIds.filter { it !in remoteIds && it !in unsyncedIds }
        if (staleIds.isNotEmpty()) {
            deleteWithdrawalsByIds(staleIds)
        }
    }
}
