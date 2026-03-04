package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository

/**
 * Use case for deleting an expense from a group.
 *
 * This encapsulates the business logic for expense deletion, including:
 * - Refunding consumed cash tranches back to their original ATM withdrawals.
 * - Delegating offline-first delete to the repository.
 */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository
) {
    /**
     * Deletes an expense by its ID within a group.
     *
     * If the expense was paid in CASH, restores the consumed amounts back
     * to the respective CashWithdrawal records before deleting.
     *
     * @param groupId The ID of the group containing the expense.
     * @param expenseId The ID of the expense to delete.
     */
    suspend operator fun invoke(groupId: String, expenseId: String) {
        // Fetch the expense to check for cash tranches before deletion
        val expense = expenseRepository.getExpenseById(expenseId)

        // Refund cash tranches if this was a cash expense
        expense?.cashTranches?.forEach { tranche ->
            cashWithdrawalRepository.refundTranche(
                withdrawalId = tranche.withdrawalId,
                amountToRefund = tranche.amountConsumed
            )
        }

        expenseRepository.deleteExpense(groupId, expenseId)
    }
}

