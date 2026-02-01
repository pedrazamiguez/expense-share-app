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
        val expenseWithGroup = expense.copy(groupId = groupId)

        // 1. Save to local first - UI updates instantly via Flow
        localExpenseDataSource.saveExpense(expenseWithGroup)

        // 2. Sync to cloud in background - doesn't block UI
        syncScope.launch {
            try {
                cloudExpenseDataSource.addExpense(groupId, expenseWithGroup)
                Timber.d("Expense synced to cloud: ${expense.id}")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync expense to cloud, will retry later")
                // TODO: Implement retry queue for failed syncs
            }
        }
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
