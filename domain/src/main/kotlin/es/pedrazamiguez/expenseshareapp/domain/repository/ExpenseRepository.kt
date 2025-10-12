package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Expense

interface ExpenseRepository {
    suspend fun addExpense(
        groupId: String,
        expense: Expense
    )
}
