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
     * Non-destructively reconciles local expenses for [groupId] with a snapshot
     * from the remote source.
     *
     * Implementations MUST preserve any locally-created or locally-modified expenses
     * that have not yet been synced to the cloud (e.g. "dirty" offline writes). In
     * particular, this MUST NOT be implemented as "delete all expenses for the group,
     * then insert [expenses]", because that would drop pending local changes.
     *
     * A typical implementation strategy is to upsert (insert or update) the expenses
     * contained in [expenses], leaving any locally-dirty, unsynced expenses untouched.
     * Deletions of remote expenses are driven by explicit [deleteExpense] calls from the
     * write protocol, not by this reconciliation method.
     *
     * Callers may safely use this during real-time sync without risking the loss of
     * unsynced local writes.
     */
    suspend fun replaceExpensesForGroup(groupId: String, expenses: List<Expense>)

    suspend fun getExpenseIdsByGroup(groupId: String): List<String>

    suspend fun clearAllExpenses()
}
