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

    /**
     * Atomically replaces all expenses for a group with the provided list.
     * Used during real-time sync to reconcile local state with the cloud snapshot.
     * This handles both additions and deletions made by other users/devices.
     */
    suspend fun replaceExpensesForGroup(groupId: String, expenses: List<Expense>)

    suspend fun getExpenseIdsByGroup(groupId: String): List<String>

    suspend fun clearAllExpenses()
}
