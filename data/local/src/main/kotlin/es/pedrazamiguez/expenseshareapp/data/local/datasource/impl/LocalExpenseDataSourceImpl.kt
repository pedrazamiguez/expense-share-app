package es.pedrazamiguez.expenseshareapp.data.local.datasource.impl

import es.pedrazamiguez.expenseshareapp.data.local.dao.ExpenseDao
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toEntity
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalExpenseDataSourceImpl(
    private val expenseDao: ExpenseDao
) : LocalExpenseDataSource {

    override fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByGroupIdFlow(groupId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getExpenseById(expenseId: String): Expense? {
        return expenseDao.getExpenseById(expenseId)?.toDomain()
    }

    override suspend fun saveExpenses(expenses: List<Expense>) {
        expenseDao.insertExpenses(expenses.toEntity())
    }

    override suspend fun saveExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expenseId: String) {
        expenseDao.deleteExpense(expenseId)
    }

    override suspend fun deleteExpensesByGroupId(groupId: String) {
        expenseDao.deleteExpensesByGroupId(groupId)
    }

    override suspend fun clearAllExpenses() {
        expenseDao.clearAllExpenses()
    }
}
