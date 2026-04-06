package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface CloudExpenseDataSource {
    suspend fun addExpense(groupId: String, expense: Expense)
    suspend fun deleteExpense(groupId: String, expenseId: String)

    /**
     * One-shot fetch of expenses for sync purposes.
     * Backed by a Firestore .get().await() call that uses the default source
     * (server when available, but may fall back to the local cache).
     * Exceptions propagate to the caller; use this for background sync operations
     * instead of the reactive Flow.
     */
    suspend fun fetchExpensesByGroupId(groupId: String): List<Expense>

    /**
     * Reactive stream of expenses for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>>
}
