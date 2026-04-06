package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudSubunitDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalSubunitDataSource
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import java.time.LocalDateTime
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

class SubunitRepositoryImpl(
    private val cloudSubunitDataSource: CloudSubunitDataSource,
    private val localSubunitDataSource: LocalSubunitDataSource,
    private val authenticationService: AuthenticationService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SubunitRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

    /**
     * Tracks active cloud subscription Jobs per groupId.
     * Prevents duplicate Firestore snapshot listeners from accumulating
     * when onStart fires multiple times (e.g., config changes, tab switches,
     * WhileSubscribed resubscriptions, flatMapLatest restarts).
     */
    private val cloudSubscriptionJobs = ConcurrentHashMap<String, Job>()

    override suspend fun createSubunit(groupId: String, subunit: Subunit): String {
        val subunitId = subunit.id.ifBlank { UUID.randomUUID().toString() }
        val currentUserId = authenticationService.currentUserId() ?: ""
        val currentTimestamp = LocalDateTime.now()

        val subunitWithMetadata = subunit.copy(
            id = subunitId,
            groupId = groupId,
            createdBy = subunit.createdBy.ifBlank { currentUserId },
            createdAt = subunit.createdAt ?: currentTimestamp,
            lastUpdatedAt = currentTimestamp,
            syncStatus = SyncStatus.PENDING_SYNC
        )

        // Save to local first - UI updates instantly via Flow
        localSubunitDataSource.saveSubunit(subunitWithMetadata)

        // Sync to cloud in background
        syncScope.launch {
            try {
                cloudSubunitDataSource.addSubunit(groupId, subunitWithMetadata)
                localSubunitDataSource.updateSyncStatus(subunitWithMetadata.id, SyncStatus.SYNCED)
                Timber.d("Subunit synced to cloud: ${subunitWithMetadata.id}")
            } catch (e: Exception) {
                localSubunitDataSource.updateSyncStatus(subunitWithMetadata.id, SyncStatus.SYNC_FAILED)
                Timber.w(e, "Failed to sync subunit to cloud")
            }
        }

        return subunitId
    }

    override suspend fun updateSubunit(groupId: String, subunit: Subunit) {
        val currentTimestamp = LocalDateTime.now()

        val updatedSubunit = subunit.copy(
            groupId = groupId,
            lastUpdatedAt = currentTimestamp,
            syncStatus = SyncStatus.PENDING_SYNC
        )

        // Save to local first (upsert) - UI updates instantly via Flow
        localSubunitDataSource.saveSubunit(updatedSubunit)

        // Sync to cloud in background
        syncScope.launch {
            try {
                cloudSubunitDataSource.updateSubunit(groupId, updatedSubunit)
                localSubunitDataSource.updateSyncStatus(updatedSubunit.id, SyncStatus.SYNCED)
                Timber.d("Subunit update synced to cloud: ${updatedSubunit.id}")
            } catch (e: Exception) {
                localSubunitDataSource.updateSyncStatus(updatedSubunit.id, SyncStatus.SYNC_FAILED)
                Timber.w(e, "Failed to sync subunit update to cloud")
            }
        }
    }

    override suspend fun deleteSubunit(groupId: String, subunitId: String) {
        // Delete from local first - UI updates instantly via Flow
        localSubunitDataSource.deleteSubunit(subunitId)

        // Sync deletion to cloud in background
        syncScope.launch {
            try {
                cloudSubunitDataSource.deleteSubunit(groupId, subunitId)
                Timber.d("Subunit deletion synced to cloud: $subunitId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync subunit deletion to cloud, will retry later")
            }
        }
    }

    /**
     * Returns a Flow of subunits for a group from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     *
     * Uses a single shared subscription per groupId: any existing cloud listener
     * for this group is cancelled before starting a new one, preventing duplicate
     * snapshot listeners from accumulating across flatMapLatest restarts,
     * config changes, or WhileSubscribed resubscriptions.
     */
    override fun getGroupSubunitsFlow(groupId: String): Flow<List<Subunit>> =
        localSubunitDataSource.getSubunitsByGroupIdFlow(groupId)
            .onStart {
                // Cancel any previous cloud subscription for this group to prevent duplicates.
                cloudSubscriptionJobs[groupId]?.cancel()
                cloudSubscriptionJobs[groupId] = syncScope.launch {
                    subscribeToCloudChanges(groupId)
                }
            }

    override suspend fun getSubunitById(subunitId: String): Subunit? = localSubunitDataSource.getSubunitById(subunitId)

    override suspend fun getGroupSubunits(groupId: String): List<Subunit> =
        localSubunitDataSource.getSubunitsByGroupId(groupId)

    /**
     * Subscribes to real-time Firestore snapshot changes for a group's subunits.
     *
     * The Firestore snapshotListener fires whenever ANY user adds, modifies, or
     * deletes a subunit in this group. Each snapshot represents the complete
     * authoritative state of the collection.
     *
     * We use [replaceSubunitsForGroup] with a merge reconciliation strategy
     * (upsert remote + selective delete of stale) to safely reconcile the
     * local DB with the cloud snapshot.
     */
    private suspend fun subscribeToCloudChanges(groupId: String) {
        try {
            cloudSubunitDataSource.getSubunitsByGroupIdFlow(groupId)
                .collect { remoteSubunits ->
                    try {
                        Timber.d("Real-time sync: ${remoteSubunits.size} subunits for group $groupId")
                        localSubunitDataSource.replaceSubunitsForGroup(
                            groupId,
                            remoteSubunits
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling subunits from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud subunit changes, using local cache")
        }
    }
}
