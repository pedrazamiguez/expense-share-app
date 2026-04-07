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

    /**
     * Verifies that an expense document exists on the Firestore server (not just local cache).
     * Forces a server round-trip — throws if the device is offline.
     *
     * Used by repositories to confirm that a locally-created expense has been
     * successfully persisted to the server, enabling the PENDING_SYNC → SYNCED transition.
     *
     * @param groupId The ID of the group containing the expense.
     * @param expenseId The ID of the expense to verify.
     * @return true if the expense exists on the server.
     * @throws Exception if the server is unreachable (e.g., airplane mode).
     */
    suspend fun verifyExpenseOnServer(groupId: String, expenseId: String): Boolean
}
