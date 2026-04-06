package es.pedrazamiguez.splittrip.domain.datasource.local

import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import kotlinx.coroutines.flow.Flow

interface LocalCashWithdrawalDataSource {

    fun getWithdrawalsByGroupIdFlow(groupId: String): Flow<List<CashWithdrawal>>

    /**
     * Fetches available (non-exhausted) withdrawals for a specific currency,
     * ordered by createdAt ascending (oldest first) for FIFO consumption.
     */
    suspend fun getAvailableWithdrawals(groupId: String, currency: String): List<CashWithdrawal>

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

    suspend fun clearAllWithdrawals()
}
