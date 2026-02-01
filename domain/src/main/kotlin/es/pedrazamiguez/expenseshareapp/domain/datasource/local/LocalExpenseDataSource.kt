package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface LocalExpenseDataSource {

    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>>

    suspend fun getExpenseById(expenseId: String): Expense?

    suspend fun saveExpenses(expenses: List<Expense>)

    suspend fun saveExpense(expense: Expense)

    suspend fun deleteExpense(expenseId: String)

    suspend fun deleteExpensesByGroupId(groupId: String)

    suspend fun clearAllExpenses()
}
