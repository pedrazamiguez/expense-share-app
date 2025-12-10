package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(
        groupId: String,
        expense: Expense
    )

    fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>>
}
