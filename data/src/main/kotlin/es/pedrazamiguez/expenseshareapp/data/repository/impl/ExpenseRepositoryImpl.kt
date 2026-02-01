package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class ExpenseRepositoryImpl(
    private val cloudExpenseDataSource: CloudExpenseDataSource,
    private val localExpenseDataSource: LocalExpenseDataSource,
    private val authenticationService: AuthenticationService
) : ExpenseRepository {

    private val syncScope = CoroutineScope(Dispatchers.IO)

    override suspend fun addExpense(groupId: String, expense: Expense) {
        val expenseId = expense.id.ifBlank { UUID.randomUUID().toString() }
        val currentUserId = authenticationService.currentUserId() ?: ""
        val currentTimestamp = java.time.LocalDateTime.now()

        val expenseWithMetadata = expense.copy(
            id = expenseId,
            groupId = groupId,
            createdBy = expense.createdBy.ifBlank { currentUserId },
            createdAt = expense.createdAt ?: currentTimestamp,
            lastUpdatedAt = currentTimestamp
        )

        // Save to local first - UI updates instantly via Flow
        localExpenseDataSource.saveExpense(expenseWithMetadata)

        // Sync to cloud in background
        syncScope.launch {
            try {
                cloudExpenseDataSource.addExpense(groupId, expenseWithMetadata)
                Timber.d("Expense synced to cloud: ${expenseWithMetadata.id}")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync expense to cloud, will retry later")
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
            Timber.d("Received ${remoteExpenses.size} expenses from cloud, merging with local")

            // Merge cloud data with local using UPSERT to avoid overwriting unsync'd expenses
            localExpenseDataSource.saveExpenses(remoteExpenses)
        } catch (e: Exception) {
            Timber.w(e, "Error syncing expenses from cloud, using local cache")
        }
    }
}
