package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
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

class CashWithdrawalRepositoryImpl(
    private val cloudCashWithdrawalDataSource: CloudCashWithdrawalDataSource,
    private val localCashWithdrawalDataSource: LocalCashWithdrawalDataSource,
    private val authenticationService: AuthenticationService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CashWithdrawalRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

    /**
     * Tracks active cloud subscription Jobs per groupId.
     * Prevents duplicate Firestore snapshot listeners from accumulating
     * when onStart fires multiple times (e.g., config changes, tab switches,
     * WhileSubscribed resubscriptions, flatMapLatest restarts).
     */
    private val cloudSubscriptionJobs = ConcurrentHashMap<String, Job>()

    override suspend fun addWithdrawal(groupId: String, withdrawal: CashWithdrawal) {
        val withdrawalId = withdrawal.id.ifBlank { UUID.randomUUID().toString() }
        val currentUserId = authenticationService.currentUserId() ?: ""
        val currentTimestamp = java.time.LocalDateTime.now()

        val withdrawalWithMetadata = withdrawal.copy(
            id = withdrawalId,
            groupId = groupId,
            withdrawnBy = withdrawal.withdrawnBy.ifBlank { currentUserId },
            remainingAmount = withdrawal.remainingAmount.takeIf { it > 0 }
                ?: withdrawal.amountWithdrawn,
            createdAt = withdrawal.createdAt ?: currentTimestamp,
            lastUpdatedAt = currentTimestamp
        )

        // Save to local first - UI updates instantly via Flow
        localCashWithdrawalDataSource.saveWithdrawal(withdrawalWithMetadata)

        // Sync to cloud in background
        syncScope.launch {
            try {
                cloudCashWithdrawalDataSource.addWithdrawal(groupId, withdrawalWithMetadata)
                Timber.d("Cash withdrawal synced to cloud: ${withdrawalWithMetadata.id}")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync cash withdrawal to cloud, will retry later")
            }
        }
    }

    /**
     * Returns a Flow of cash withdrawals for a group from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     *
     * Uses a single shared subscription per groupId: any existing cloud listener
     * for this group is cancelled before starting a new one, preventing duplicate
     * snapshot listeners from accumulating across flatMapLatest restarts,
     * config changes, or WhileSubscribed resubscriptions.
     */
    override fun getGroupWithdrawalsFlow(groupId: String): Flow<List<CashWithdrawal>> =
        localCashWithdrawalDataSource.getWithdrawalsByGroupIdFlow(groupId)
            .onStart {
                // Cancel any previous cloud subscription for this group to prevent duplicates.
                cloudSubscriptionJobs[groupId]?.cancel()
                cloudSubscriptionJobs[groupId] = syncScope.launch {
                    subscribeToCloudChanges(groupId)
                }
            }

    override suspend fun getAvailableWithdrawals(groupId: String, currency: String): List<CashWithdrawal> =
        localCashWithdrawalDataSource.getAvailableWithdrawals(groupId, currency)

    override suspend fun updateRemainingAmount(withdrawalId: String, newRemaining: Long) {
        localCashWithdrawalDataSource.updateRemainingAmount(withdrawalId, newRemaining)

        // Sync updated withdrawal to cloud in background
        syncScope.launch {
            try {
                val withdrawal = localCashWithdrawalDataSource.getWithdrawalById(withdrawalId)
                if (withdrawal != null) {
                    cloudCashWithdrawalDataSource.updateWithdrawal(
                        withdrawal.groupId,
                        withdrawal
                    )
                    Timber.d("Cash withdrawal update synced to cloud: $withdrawalId")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync cash withdrawal update to cloud")
            }
        }
    }

    override suspend fun updateRemainingAmounts(groupId: String, withdrawals: List<CashWithdrawal>) {
        // Batch all local DB updates in a single transaction
        val updates = withdrawals.map { it.id to it.remainingAmount }
        localCashWithdrawalDataSource.updateRemainingAmounts(updates)

        // Sync all updated withdrawals to the cloud in a single background job — no extra
        // local reads needed since the full CashWithdrawal objects are already available.
        syncScope.launch {
            for (withdrawal in withdrawals) {
                try {
                    cloudCashWithdrawalDataSource.updateWithdrawal(groupId, withdrawal)
                    Timber.d("Cash withdrawal update synced to cloud: ${withdrawal.id}")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to sync cash withdrawal update to cloud: ${withdrawal.id}")
                }
            }
        }
    }

    override suspend fun refundTranche(withdrawalId: String, amountToRefund: Long) {
        val withdrawal = localCashWithdrawalDataSource.getWithdrawalById(withdrawalId)
        if (withdrawal != null) {
            val newRemaining = withdrawal.remainingAmount + amountToRefund
            updateRemainingAmount(withdrawalId, newRemaining)
        } else {
            Timber.w(
                "Skipping cash withdrawal refund: withdrawal not found locally. " +
                    "withdrawalId=%s, amountToRefund=%d",
                withdrawalId,
                amountToRefund
            )
        }
    }

    override suspend fun deleteWithdrawal(groupId: String, withdrawalId: String) {
        // Delete from local first - UI updates instantly via Flow
        localCashWithdrawalDataSource.deleteWithdrawal(withdrawalId)

        // Sync deletion to cloud in background
        syncScope.launch {
            try {
                cloudCashWithdrawalDataSource.deleteWithdrawal(groupId, withdrawalId)
                Timber.d("Cash withdrawal deletion synced to cloud: $withdrawalId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync cash withdrawal deletion to cloud, will retry later")
            }
        }
    }

    /**
     * Subscribes to real-time Firestore snapshot changes for a group's cash withdrawals.
     *
     * The Firestore snapshotListener fires whenever ANY user adds, modifies, or
     * deletes a withdrawal in this group. Each snapshot represents the complete
     * authoritative state of the collection.
     *
     * We use [replaceWithdrawalsForGroup] with a merge reconciliation strategy
     * (upsert remote + selective delete of stale) to safely reconcile the
     * local DB with the cloud snapshot.
     */
    private suspend fun subscribeToCloudChanges(groupId: String) {
        try {
            cloudCashWithdrawalDataSource.getWithdrawalsByGroupIdFlow(groupId)
                .collect { remoteWithdrawals ->
                    try {
                        Timber.d("Real-time sync: ${remoteWithdrawals.size} cash withdrawals for group $groupId")
                        localCashWithdrawalDataSource.replaceWithdrawalsForGroup(
                            groupId,
                            remoteWithdrawals
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling cash withdrawals from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud cash withdrawal changes, using local cache")
        }
    }
}
