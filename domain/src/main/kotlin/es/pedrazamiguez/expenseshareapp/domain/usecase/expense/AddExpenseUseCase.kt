package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(
        groupId: String,
        expense: Expense
    ): Result<Unit> = runCatching {

        require(expense.amountCents > 0) { "Amount must be positive" }
        require(expense.title.isNotBlank()) { "Title required" }

        expenseRepository.addExpense(
            groupId,
            expense
        )
    }

}
