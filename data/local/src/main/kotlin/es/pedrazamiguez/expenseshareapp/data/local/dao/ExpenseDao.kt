package es.pedrazamiguez.expenseshareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAtMillis DESC")
    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("DELETE FROM expenses WHERE groupId = :groupId")
    suspend fun deleteExpensesByGroupId(groupId: String)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}
