package es.pedrazamiguez.splittrip.domain.usecase.expense

import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetGroupExpensesFlowUseCase(private val expenseRepository: ExpenseRepository) {
    operator fun invoke(groupId: String): Flow<List<Expense>> = expenseRepository.getGroupExpensesFlow(groupId)
}
