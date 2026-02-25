package es.pedrazamiguez.expenseshareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
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
}
