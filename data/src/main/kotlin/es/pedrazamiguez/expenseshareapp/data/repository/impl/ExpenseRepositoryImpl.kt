package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class ExpenseRepositoryImpl(
    private val cloudExpenseDataSource: CloudExpenseDataSource,
    private val localExpenseDataSource: LocalExpenseDataSource,
    private val authenticationService: AuthenticationService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ExpenseRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

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

    override suspend fun deleteExpense(groupId: String, expenseId: String) {
        // Delete from local first - UI updates instantly via Flow
        localExpenseDataSource.deleteExpense(expenseId)

        // Sync deletion to cloud in background
        syncScope.launch {
            try {
                cloudExpenseDataSource.deleteExpense(groupId, expenseId)
                Timber.d("Expense deletion synced to cloud: $expenseId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync expense deletion to cloud, will retry later")
            }
        }
    }

    override fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>> = channelFlow {
        // Forward local Room data to downstream collectors.
        // This is the Single Source of Truth for the UI.
        launch {
            try {
                localExpenseDataSource.getExpensesByGroupIdFlow(groupId).collect { send(it) }
            } catch (e: Exception) {
                Timber.e(e, "Error reading local expenses for group $groupId")
            }
        }

        // Cloud subscription - lifecycle is tied to this flow's collection.
        // When the collector cancels (e.g., group switch), this subscription is
        // also cancelled, preventing orphan listeners or duplicate reconciliation.
        try {
            cloudExpenseDataSource.getExpensesByGroupIdFlow(groupId)
                .collect { remoteExpenses ->
                    try {
                        Timber.d("Real-time sync: ${remoteExpenses.size} expenses for group $groupId")
                        localExpenseDataSource.replaceExpensesForGroup(groupId, remoteExpenses)
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling expenses from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud expense changes, using local cache")
        }
    }
}
