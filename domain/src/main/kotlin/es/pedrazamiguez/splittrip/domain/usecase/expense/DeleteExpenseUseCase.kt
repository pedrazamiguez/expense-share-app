package es.pedrazamiguez.splittrip.domain.usecase.expense

import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService

/**
 * Use case for deleting an expense from a group.
 *
 * This encapsulates the business logic for expense deletion, including:
 * - Membership validation (user must belong to the group).
 * - Refunding consumed cash tranches back to their original ATM withdrawals.
 * - Cascade-deleting any linked paired contribution (out-of-pocket expenses).
 * - Delegating offline-first delete to the repository.
 */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val groupMembershipService: GroupMembershipService,
    private val contributionRepository: ContributionRepository
) {
    /**
     * Deletes an expense by its ID within a group.
     *
     * If the expense was paid in CASH, restores the consumed amounts back
     * to the respective CashWithdrawal records before deleting.
     *
     * If the expense was out-of-pocket (payerType = USER), cascade-deletes
     * the linked paired contribution. This is safe to call for GROUP expenses
     * too — [ContributionRepository.deleteByLinkedExpenseId] is a graceful no-op
     * when no linked contribution exists.
     *
     * @param groupId The ID of the group containing the expense.
     * @param expenseId The ID of the expense to delete.
     * @throws NotGroupMemberException if the user is not a member of the group.
     */
    suspend operator fun invoke(groupId: String, expenseId: String) {
        groupMembershipService.requireMembership(groupId)

        // Fetch the expense to check for cash tranches before deletion
        val expense = expenseRepository.getExpenseById(expenseId)

        // Refund cash tranches if this was a cash expense
        expense?.cashTranches?.forEach { tranche ->
            cashWithdrawalRepository.refundTranche(
                withdrawalId = tranche.withdrawalId,
                amountToRefund = tranche.amountConsumed
            )
        }

        // Cascade-delete linked paired contribution (no-op if none exists)
        contributionRepository.deleteByLinkedExpenseId(groupId, expenseId)

        expenseRepository.deleteExpense(groupId, expenseId)
    }
}
