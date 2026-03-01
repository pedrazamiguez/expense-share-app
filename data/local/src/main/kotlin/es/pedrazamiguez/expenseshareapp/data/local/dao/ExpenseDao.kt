package es.pedrazamiguez.expenseshareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAtMillis DESC")
    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Upsert
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Upsert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("DELETE FROM expenses WHERE groupId = :groupId")
    suspend fun deleteExpensesByGroupId(groupId: String)

    @Query("SELECT id FROM expenses WHERE groupId = :groupId")
    suspend fun getExpenseIdsByGroupId(groupId: String): List<String>

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    /**
     * Deletes expenses whose IDs are in the provided list.
     * Used to selectively remove stale expenses during sync reconciliation.
     */
    @Query("DELETE FROM expenses WHERE id IN (:ids)")
    suspend fun deleteExpensesByIds(ids: List<String>)

    /**
     * Reconciles local expenses for a group with the authoritative cloud snapshot.
     *
     * Uses a merge strategy instead of destructive delete+insert:
     * 1. Upsert all remote expenses (adds new, updates existing)
     * 2. Delete only local expenses whose IDs are NOT in the remote set
     *
     * This preserves locally-created expenses that haven't synced to the cloud yet.
     * The Firestore SDK's latency compensation includes pending writes in snapshots,
     * so this race is extremely narrow, but the merge strategy provides an extra
     * safety net to prevent data loss of unsynced offline changes.
     */
    @Transaction
    suspend fun replaceExpensesForGroup(groupId: String, expenses: List<ExpenseEntity>) {
        val remoteIds = expenses.map { it.id }.toSet()
        val localIds = getExpenseIdsByGroupId(groupId)
        val staleIds = localIds.filter { it !in remoteIds }

        // 1. Upsert remote expenses (adds new ones, updates existing)
        insertExpenses(expenses)

        // 2. Remove only stale expenses (exist locally but not in remote snapshot)
        if (staleIds.isNotEmpty()) {
            deleteExpensesByIds(staleIds)
        }
    }
}
