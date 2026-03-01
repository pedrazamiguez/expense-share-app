package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository

/**
 * Use case for deleting an expense from a group.
 *
 * This encapsulates the business logic for expense deletion.
 * Future enhancements could include:
 * - Permission checks (e.g., "Only the creator or Admin can delete")
 * - Pre-deletion validation
 * - Audit logging
 */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    /**
     * Deletes an expense by its ID within a group.
     *
     * The repository handles the offline-first strategy:
     * delete locally first for instant UI feedback, then sync to cloud in background.
     *
     * @param groupId The ID of the group containing the expense.
     * @param expenseId The ID of the expense to delete.
     */
    suspend operator fun invoke(groupId: String, expenseId: String) {
        expenseRepository.deleteExpense(groupId, expenseId)
    }
}

