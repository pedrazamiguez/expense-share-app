package es.pedrazamiguez.splittrip.domain.datasource.local

import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import kotlinx.coroutines.flow.Flow

interface LocalCashWithdrawalDataSource {

    fun getWithdrawalsByGroupIdFlow(groupId: String): Flow<List<CashWithdrawal>>

    /**
     * Fetches available (non-exhausted) withdrawals for a specific currency,
     * ordered by createdAt ascending (oldest first) for FIFO consumption.
     *
     * **Scope-blind:** returns all withdrawals regardless of scope.
     * Prefer the scoped variants below for FIFO pool selection in expense processing.
     */
    suspend fun getAvailableWithdrawals(groupId: String, currency: String): List<CashWithdrawal>

    /**
     * Fetches available GROUP-scoped withdrawals for FIFO consumption.
     * Ordered by createdAt ascending (oldest first).
     */
    suspend fun getAvailableWithdrawalsGroupScoped(
        groupId: String,
        currency: String
    ): List<CashWithdrawal>

    /**
     * Fetches available USER-scoped withdrawals for a specific user, for FIFO consumption.
     * Ordered by createdAt ascending (oldest first).
     */
    suspend fun getAvailableWithdrawalsUserScoped(
        groupId: String,
        currency: String,
        withdrawnBy: String
    ): List<CashWithdrawal>

    /**
     * Fetches available SUBUNIT-scoped withdrawals for a specific subunit, for FIFO consumption.
     * Ordered by createdAt ascending (oldest first).
     */
    suspend fun getAvailableWithdrawalsSubunitScoped(
        groupId: String,
        currency: String,
        subunitId: String
    ): List<CashWithdrawal>

    suspend fun getWithdrawalById(withdrawalId: String): CashWithdrawal?

    suspend fun saveWithdrawal(withdrawal: CashWithdrawal)

    suspend fun updateRemainingAmount(withdrawalId: String, newRemaining: Long)

    /**
     * Atomically updates the remaining amount on multiple withdrawals in a single transaction.
     * Used during FIFO cash expense processing to batch all tranche deductions together.
     */
    suspend fun updateRemainingAmounts(updates: List<Pair<String, Long>>)

    suspend fun deleteWithdrawal(withdrawalId: String)

    suspend fun deleteWithdrawalsByGroupId(groupId: String)

    /**
     * Atomically replaces all withdrawals for a group with the provided list.
     * Used during real-time sync to reconcile local state with the cloud snapshot.
     */
    suspend fun replaceWithdrawalsForGroup(groupId: String, withdrawals: List<CashWithdrawal>)

    suspend fun getWithdrawalIdsByGroup(groupId: String): List<String>

    /**
     * Updates the sync status of a single cash withdrawal.
     * Used by repositories to track cloud sync progress (PENDING_SYNC → SYNCED / SYNC_FAILED).
     */
    suspend fun updateSyncStatus(withdrawalId: String, syncStatus: SyncStatus)

    /**
     * Returns IDs of withdrawals in a group that are waiting for server confirmation.
     * Used by the repository after reconciliation to attempt server verification
     * and transition PENDING_SYNC items to SYNCED.
     */
    suspend fun getPendingSyncWithdrawalIds(groupId: String): List<String>

    suspend fun clearAllWithdrawals()
}
