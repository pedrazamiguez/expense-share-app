package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import kotlinx.coroutines.flow.Flow

interface CashWithdrawalRepository {

    suspend fun addWithdrawal(groupId: String, withdrawal: CashWithdrawal)

    fun getGroupWithdrawalsFlow(groupId: String): Flow<List<CashWithdrawal>>

    /**
     * Fetches available (non-exhausted) withdrawals for a specific currency,
     * ordered by createdAt ascending (oldest first) for FIFO consumption.
     */
    suspend fun getAvailableWithdrawals(groupId: String, currency: String): List<CashWithdrawal>

    /**
     * Updates the remaining amount on a withdrawal after FIFO consumption or refund.
     */
    suspend fun updateRemainingAmount(withdrawalId: String, newRemaining: Long)

    /**
     * Atomically updates the remaining amounts on multiple withdrawals in a single local DB
     * transaction, then syncs all changes to the cloud in one background job.
     * Preferred over calling [updateRemainingAmount] in a loop for multi-tranche cash expenses.
     *
     * @param groupId The group the withdrawals belong to (needed for cloud sync).
     * @param withdrawals The updated [CashWithdrawal] objects with their new [CashWithdrawal.remainingAmount] already applied.
     */
    suspend fun updateRemainingAmounts(groupId: String, withdrawals: List<CashWithdrawal>)

    /**
     * Refunds a previously consumed tranche back to its withdrawal.
     * Adds amountToRefund to the withdrawal's current remainingAmount.
     */
    suspend fun refundTranche(withdrawalId: String, amountToRefund: Long)

    suspend fun deleteWithdrawal(groupId: String, withdrawalId: String)
}


