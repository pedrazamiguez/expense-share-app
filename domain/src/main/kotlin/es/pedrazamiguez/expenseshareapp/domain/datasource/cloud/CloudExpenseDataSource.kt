package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface CloudExpenseDataSource {
    suspend fun addExpense(groupId: String, expense: Expense)
    suspend fun deleteExpense(groupId: String, expenseId: String)

    /**
     * One-shot fetch of expenses from the server for sync purposes.
     * Uses .get().await() to wait for the actual server response.
     * Use this for background sync operations instead of the reactive Flow.
     */
    suspend fun fetchExpensesByGroupId(groupId: String): List<Expense>

    /**
     * Reactive stream of expenses for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>>
}
