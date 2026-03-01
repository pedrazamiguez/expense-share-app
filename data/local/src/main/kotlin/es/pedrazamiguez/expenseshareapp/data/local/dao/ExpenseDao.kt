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
     * Atomically replaces all expenses for a group with the cloud snapshot.
     *
     * This is used during real-time sync to reconcile the local database with
     * the authoritative cloud state. The @Transaction ensures no intermediate
     * state is visible to Room Flow observers (no flicker).
     *
     * Important: This should ONLY be called with cloud data during sync.
     * Local-only writes (offline additions) go through [insertExpense].
     */
    @Transaction
    suspend fun replaceExpensesForGroup(groupId: String, expenses: List<ExpenseEntity>) {
        deleteExpensesByGroupId(groupId)
        insertExpenses(expenses)
    }
}
