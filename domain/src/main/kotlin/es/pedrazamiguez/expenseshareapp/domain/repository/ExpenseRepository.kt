package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(groupId: String, expense: Expense)

    suspend fun getExpenseById(expenseId: String): Expense?

    fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>>

    suspend fun deleteExpense(groupId: String, expenseId: String)
}
