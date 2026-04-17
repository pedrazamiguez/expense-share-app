package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.sync.KeyedSubscriptionTracker
import es.pedrazamiguez.splittrip.data.sync.subscribeAndReconcile
import es.pedrazamiguez.splittrip.data.sync.syncCreateToCloud
import es.pedrazamiguez.splittrip.data.sync.syncDeletionToCloud
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudCashWithdrawalDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val subscriptionTracker = KeyedSubscriptionTracker()

    override suspend fun addWithdrawal(groupId: String, withdrawal: CashWithdrawal) {
        val withdrawalId = withdrawal.id.ifBlank { UUID.randomUUID().toString() }
        val currentUserId = authenticationService.currentUserId() ?: ""
        val currentTimestamp = java.time.LocalDateTime.now()

        val withdrawalWithMetadata = withdrawal.copy(
            id = withdrawalId,
            groupId = groupId,
            withdrawnBy = withdrawal.withdrawnBy.ifBlank { currentUserId },
            createdBy = currentUserId,
            remainingAmount = withdrawal.remainingAmount.takeIf { it > 0 }
                ?: withdrawal.amountWithdrawn,
            createdAt = withdrawal.createdAt ?: currentTimestamp,
            lastUpdatedAt = currentTimestamp,
            syncStatus = SyncStatus.PENDING_SYNC
        )

        localCashWithdrawalDataSource.saveWithdrawal(withdrawalWithMetadata)

        syncCreateToCloud(
            scope = syncScope,
            entityId = withdrawalWithMetadata.id,
            cloudWrite = {
                cloudCashWithdrawalDataSource.addWithdrawal(groupId, withdrawalWithMetadata)
            },
            updateSyncStatus = localCashWithdrawalDataSource::updateSyncStatus,
            getCurrentSyncStatus = { id ->
                localCashWithdrawalDataSource.getWithdrawalById(id)?.syncStatus
                    ?: SyncStatus.PENDING_SYNC
            },
            entityLabel = ENTITY_LABEL
        )
    }

    /**
     * Returns a Flow of cash withdrawals for a group from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     *
     * Uses [KeyedSubscriptionTracker] to enforce a single shared subscription
     * per groupId, preventing duplicate snapshot listeners from accumulating
     * across flatMapLatest restarts, config changes, or WhileSubscribed
     * resubscriptions.
     */
    override fun getGroupWithdrawalsFlow(groupId: String): Flow<List<CashWithdrawal>> =
        localCashWithdrawalDataSource.getWithdrawalsByGroupIdFlow(groupId)
            .onStart {
                subscriptionTracker.cancelAndRelaunch(groupId, syncScope) {
                    subscribeAndReconcile(
                        cloudFlow = cloudCashWithdrawalDataSource
                            .getWithdrawalsByGroupIdFlow(groupId),
                        reconcileLocal = { remoteWithdrawals ->
                            localCashWithdrawalDataSource.replaceWithdrawalsForGroup(
                                groupId,
                                remoteWithdrawals
                            )
                        },
                        getPendingIds = {
                            localCashWithdrawalDataSource.getPendingSyncWithdrawalIds(groupId)
                        },
                        verifyOnServer = { id ->
                            cloudCashWithdrawalDataSource.verifyWithdrawalOnServer(groupId, id)
                        },
                        markSynced = { id ->
                            localCashWithdrawalDataSource.updateSyncStatus(id, SyncStatus.SYNCED)
                        },
                        entityLabel = ENTITY_LABEL,
                        logContext = "for group $groupId"
                    )
                }
            }

    override suspend fun getAvailableWithdrawals(
        groupId: String,
        currency: String,
        payerType: PayerType,
        payerId: String?
    ): List<CashWithdrawal> = when (payerType) {
        PayerType.GROUP ->
            localCashWithdrawalDataSource.getAvailableWithdrawalsGroupScoped(groupId, currency)

        PayerType.USER -> {
            val userPool = if (!payerId.isNullOrBlank()) {
                localCashWithdrawalDataSource.getAvailableWithdrawalsUserScoped(
                    groupId,
                    currency,
                    payerId
                )
            } else {
                emptyList()
            }
            val groupFallback = localCashWithdrawalDataSource.getAvailableWithdrawalsGroupScoped(
                groupId,
                currency
            )
            userPool + groupFallback
        }

        PayerType.SUBUNIT -> {
            val subunitPool = if (!payerId.isNullOrBlank()) {
                localCashWithdrawalDataSource.getAvailableWithdrawalsSubunitScoped(
                    groupId,
                    currency,
                    payerId
                )
            } else {
                emptyList()
            }
            val groupFallback = localCashWithdrawalDataSource.getAvailableWithdrawalsGroupScoped(
                groupId,
                currency
            )
            subunitPool + groupFallback
        }
    }

    override suspend fun getAvailableWithdrawalsByExactScope(
        groupId: String,
        currency: String,
        scope: PayerType,
        scopeOwnerId: String?
    ): List<CashWithdrawal> = when (scope) {
        PayerType.GROUP ->
            localCashWithdrawalDataSource.getAvailableWithdrawalsGroupScoped(groupId, currency)

        PayerType.USER ->
            if (!scopeOwnerId.isNullOrBlank()) {
                localCashWithdrawalDataSource.getAvailableWithdrawalsUserScoped(
                    groupId,
                    currency,
                    scopeOwnerId
                )
            } else {
                emptyList()
            }

        PayerType.SUBUNIT ->
            if (!scopeOwnerId.isNullOrBlank()) {
                localCashWithdrawalDataSource.getAvailableWithdrawalsSubunitScoped(
                    groupId,
                    currency,
                    scopeOwnerId
                )
            } else {
                emptyList()
            }
    }

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
                    Timber.d("Cash withdrawal update synced to cloud: %s", withdrawalId)
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync cash withdrawal update to cloud")
            }
        }
    }

    override suspend fun updateRemainingAmounts(
        groupId: String,
        withdrawals: List<CashWithdrawal>
    ) {
        // Batch all local DB updates in a single transaction
        val updates = withdrawals.map { it.id to it.remainingAmount }
        localCashWithdrawalDataSource.updateRemainingAmounts(updates)

        // Sync all updated withdrawals to the cloud in a single background job — no extra
        // local reads needed since the full CashWithdrawal objects are already available.
        syncScope.launch {
            for (withdrawal in withdrawals) {
                try {
                    cloudCashWithdrawalDataSource.updateWithdrawal(groupId, withdrawal)
                    Timber.d("Cash withdrawal update synced to cloud: %s", withdrawal.id)
                } catch (e: Exception) {
                    Timber.w(
                        e,
                        "Failed to sync cash withdrawal update to cloud: %s",
                        withdrawal.id
                    )
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
        localCashWithdrawalDataSource.deleteWithdrawal(withdrawalId)

        syncDeletionToCloud(
            scope = syncScope,
            entityId = withdrawalId,
            cloudDelete = {
                cloudCashWithdrawalDataSource.deleteWithdrawal(groupId, withdrawalId)
            },
            entityLabel = ENTITY_LABEL
        )
    }

    companion object {
        private const val ENTITY_LABEL = "cash withdrawal"
    }
}
