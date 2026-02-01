package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class ExpenseRepositoryImpl(
    private val cloudExpenseDataSource: CloudExpenseDataSource,
    private val localExpenseDataSource: LocalExpenseDataSource
) : ExpenseRepository {

    private val syncScope = CoroutineScope(Dispatchers.IO)

    override suspend fun addExpense(groupId: String, expense: Expense) {
        cloudExpenseDataSource.addExpense(groupId, expense)
        localExpenseDataSource.saveExpense(expense.copy(groupId = groupId))
    }

    override fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>> {
        return localExpenseDataSource.getExpensesByGroupIdFlow(groupId)
            .onStart {
                syncScope.launch {
                    refreshExpensesFromCloud(groupId)
                }
            }
    }

    private suspend fun refreshExpensesFromCloud(groupId: String) {
        try {
            Timber.d("Starting expenses sync from cloud for group: $groupId")
            val remoteExpenses = cloudExpenseDataSource.getExpensesByGroupIdFlow(groupId).first()
            Timber.d("Received ${remoteExpenses.size} expenses from cloud, saving to local")
            localExpenseDataSource.saveExpenses(remoteExpenses)
        } catch (e: Exception) {
            Timber.w(e, "Error syncing expenses from cloud, using local cache")
        }
    }
}
