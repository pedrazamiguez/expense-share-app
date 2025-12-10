package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetGroupExpensesFlowUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(groupId: String): Flow<List<Expense>> =
        expenseRepository.getGroupExpensesFlow(groupId)
}
