package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Expense

interface CloudExpenseDataSource {
    suspend fun addExpense(groupId: String, expense: Expense)
}
