package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl(
    private val cloudExpenseDataSource: CloudExpenseDataSource,
) : ExpenseRepository {

    override suspend fun addExpense(
        groupId: String, expense: Expense
    ) = cloudExpenseDataSource.addExpense(
        groupId,
        expense
    )

    override fun getGroupExpensesFlow(
        groupId: String
    ): Flow<List<Expense>> = cloudExpenseDataSource.getExpensesByGroupIdFlow(groupId)

}
