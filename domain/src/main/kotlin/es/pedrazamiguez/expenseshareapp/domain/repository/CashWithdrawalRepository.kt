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
     * Refunds a previously consumed tranche back to its withdrawal.
     * Adds amountToRefund to the withdrawal's current remainingAmount.
     */
    suspend fun refundTranche(withdrawalId: String, amountToRefund: Long)

    suspend fun deleteWithdrawal(groupId: String, withdrawalId: String)
}


