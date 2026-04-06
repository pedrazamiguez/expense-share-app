package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class ExpenseRepositoryImpl(
    private val cloudExpenseDataSource: CloudExpenseDataSource,
    private val localExpenseDataSource: LocalExpenseDataSource,
    private val authenticationService: AuthenticationService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ExpenseRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

    /**
     * Tracks active cloud subscription Jobs per groupId.
     * Prevents duplicate Firestore snapshot listeners from accumulating
     * when onStart fires multiple times (e.g., config changes, tab switches,
     * WhileSubscribed resubscriptions, flatMapLatest restarts).
     */
    private val cloudSubscriptionJobs = ConcurrentHashMap<String, Job>()

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

    override suspend fun getExpenseById(expenseId: String): Expense? = localExpenseDataSource.getExpenseById(expenseId)

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

    /**
     * Returns a Flow of expenses for a group from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     *
     * Uses a single shared subscription per groupId: any existing cloud listener
     * for this group is cancelled before starting a new one, preventing duplicate
     * snapshot listeners from accumulating across flatMapLatest restarts,
     * config changes, or WhileSubscribed resubscriptions.
     */
    override fun getGroupExpensesFlow(groupId: String): Flow<List<Expense>> =
        localExpenseDataSource.getExpensesByGroupIdFlow(groupId)
            .onStart {
                // Cancel any previous cloud subscription for this group to prevent duplicates.
                cloudSubscriptionJobs[groupId]?.cancel()
                cloudSubscriptionJobs[groupId] = syncScope.launch {
                    subscribeToCloudChanges(groupId)
                }
            }

    /**
     * Subscribes to real-time Firestore snapshot changes for a group's expenses.
     *
     * The Firestore snapshotListener fires whenever ANY user adds, modifies, or
     * deletes an expense in this group. Each snapshot represents the complete
     * authoritative state of the collection.
     *
     * We use [replaceExpensesForGroup] with a merge reconciliation strategy
     * (upsert remote + selective delete of stale) to safely reconcile the
     * local DB with the cloud snapshot. This handles:
     * - Additions by other users → new items appear locally
     * - Deletions by other users → stale items are removed locally
     * - Modifications by other users → items are updated locally
     * - Locally-created expenses not yet synced → preserved (not deleted)
     *
     * The Room Flow re-emits automatically after each reconciliation,
     * keeping the UI in sync across all devices in near real-time.
     */
    private suspend fun subscribeToCloudChanges(groupId: String) {
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
