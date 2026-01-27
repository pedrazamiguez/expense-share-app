package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(
        groupId: String?,
        expense: Expense
    ): Result<Unit> = runCatching {

        require(!groupId.isNullOrBlank()) { "Group ID cannot be null or blank" }
        require(expense.sourceAmount > 0) { "Expense amount must be greater than zero" }
        require(expense.title.isNotBlank()) { "Expense title cannot be empty" }

        expenseRepository.addExpense(
            groupId,
            expense
        )
    }

}
