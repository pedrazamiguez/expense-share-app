package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.sync.KeyedSubscriptionTracker
import es.pedrazamiguez.splittrip.data.sync.subscribeAndReconcile
import es.pedrazamiguez.splittrip.data.sync.syncCreateToCloud
import es.pedrazamiguez.splittrip.data.sync.syncDeletionToCloud
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudSettlementDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalSettlementDataSource
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class SettlementRepositoryImpl(
    private val cloudSettlementDataSource: CloudSettlementDataSource,
    private val localSettlementDataSource: LocalSettlementDataSource,
    private val authenticationService: AuthenticationService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SettlementRepository {

    private val syncScope = CoroutineScope(ioDispatcher)
    private val subscriptionTracker = KeyedSubscriptionTracker()

    override fun getGroupSettlementsFlow(groupId: String): Flow<List<SettlementRecord>> =
        localSettlementDataSource.getSettlementsByGroupIdFlow(groupId)
            .onStart {
                subscriptionTracker.cancelAndRelaunch(groupId, syncScope) {
                    subscribeAndReconcile(
                        cloudFlow = cloudSettlementDataSource.getSettlementsByGroupIdFlow(groupId),
                        reconcileLocal = { remoteRecords ->
                            localSettlementDataSource.replaceSettlementsForGroup(
                                groupId,
                                remoteRecords
                            )
                        },
                        getPendingIds = {
                            localSettlementDataSource.getPendingSyncSettlementIds(groupId)
                        },
                        verifyOnServer = { id ->
                            cloudSettlementDataSource.verifySettlementOnServer(groupId, id)
                        },
                        markSynced = { id ->
                            localSettlementDataSource.updateSyncStatus(id, SyncStatus.SYNCED)
                        },
                        entityLabel = ENTITY_LABEL,
                        logContext = "for group $groupId"
                    )
                }
            }

    override suspend fun getGroupSettlements(groupId: String): List<SettlementRecord> =
        localSettlementDataSource.getSettlementsByGroupId(groupId)

    override suspend fun getMemberSettlements(
        groupId: String,
        userId: String
    ): List<SettlementRecord> =
        localSettlementDataSource.getSettlementsByMember(groupId, userId)

    override suspend fun getSettlementById(id: String): SettlementRecord? =
        localSettlementDataSource.getSettlementById(id)

    override suspend fun addSettlement(record: SettlementRecord) {
        val currentUserId = authenticationService.currentUserId() ?: ""
        val recordWithMeta = if (record.settlement.fromUserId.isBlank()) {
            record.copy(
                settlement = record.settlement.copy(fromUserId = currentUserId)
            )
        } else {
            record
        }

        localSettlementDataSource.saveSettlement(recordWithMeta, SyncStatus.PENDING_SYNC)

        syncCreateToCloud(
            scope = syncScope,
            entityId = recordWithMeta.id,
            cloudWrite = {
                cloudSettlementDataSource.upsertSettlement(
                    recordWithMeta.groupId,
                    recordWithMeta
                )
            },
            updateSyncStatus = localSettlementDataSource::updateSyncStatus,
            getCurrentSyncStatus = { id ->
                localSettlementDataSource.getSyncStatus(id) ?: SyncStatus.PENDING_SYNC
            },
            entityLabel = ENTITY_LABEL
        )
    }

    override suspend fun updateSettlement(record: SettlementRecord) {
        localSettlementDataSource.saveSettlement(record, SyncStatus.PENDING_SYNC)

        syncCreateToCloud(
            scope = syncScope,
            entityId = record.id,
            cloudWrite = {
                cloudSettlementDataSource.upsertSettlement(
                    record.groupId,
                    record
                )
            },
            updateSyncStatus = localSettlementDataSource::updateSyncStatus,
            getCurrentSyncStatus = { id ->
                localSettlementDataSource.getSyncStatus(id) ?: SyncStatus.PENDING_SYNC
            },
            entityLabel = "$ENTITY_LABEL update"
        )
    }

    override suspend fun deleteSettlement(record: SettlementRecord) {
        localSettlementDataSource.deleteSettlement(record.id)

        syncDeletionToCloud(
            scope = syncScope,
            entityId = record.id,
            cloudDelete = {
                cloudSettlementDataSource.deleteSettlement(record.groupId, record.id)
            },
            entityLabel = ENTITY_LABEL
        )
    }

    companion object {
        private const val ENTITY_LABEL = "settlement"
    }
}
