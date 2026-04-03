package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
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

class ContributionRepositoryImpl(
    private val cloudContributionDataSource: CloudContributionDataSource,
    private val localContributionDataSource: LocalContributionDataSource,
    private val authenticationService: AuthenticationService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContributionRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

    /**
     * Tracks active cloud subscription Jobs per groupId.
     * Prevents duplicate Firestore snapshot listeners from accumulating
     * when onStart fires multiple times (e.g., config changes, tab switches,
     * WhileSubscribed resubscriptions, flatMapLatest restarts).
     */
    private val cloudSubscriptionJobs = ConcurrentHashMap<String, Job>()

    override suspend fun addContribution(groupId: String, contribution: Contribution) {
        val contributionId = contribution.id.ifBlank { UUID.randomUUID().toString() }
        val currentUserId = authenticationService.currentUserId() ?: ""
        val currentTimestamp = java.time.LocalDateTime.now()

        val contributionWithMetadata = contribution.copy(
            id = contributionId,
            groupId = groupId,
            userId = contribution.userId.ifBlank { currentUserId },
            createdBy = currentUserId,
            createdAt = contribution.createdAt ?: currentTimestamp,
            lastUpdatedAt = currentTimestamp
        )

        // Save to local first - UI updates instantly via Flow
        localContributionDataSource.saveContribution(contributionWithMetadata)

        // Sync to cloud in background
        syncScope.launch {
            try {
                cloudContributionDataSource.addContribution(groupId, contributionWithMetadata)
                Timber.d("Contribution synced to cloud: ${contributionWithMetadata.id}")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync contribution to cloud, will retry later")
            }
        }
    }

    /**
     * Returns a Flow of contributions for a group from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     *
     * Uses a single shared subscription per groupId: any existing cloud listener
     * for this group is cancelled before starting a new one, preventing duplicate
     * snapshot listeners from accumulating across flatMapLatest restarts,
     * config changes, or WhileSubscribed resubscriptions.
     */
    override fun getGroupContributionsFlow(groupId: String): Flow<List<Contribution>> =
        localContributionDataSource.getContributionsByGroupIdFlow(groupId)
            .onStart {
                // Cancel any previous cloud subscription for this group to prevent duplicates.
                cloudSubscriptionJobs[groupId]?.cancel()
                cloudSubscriptionJobs[groupId] = syncScope.launch {
                    subscribeToCloudChanges(groupId)
                }
            }

    override suspend fun deleteContribution(groupId: String, contributionId: String) {
        // Delete from local first - UI updates instantly via Flow
        localContributionDataSource.deleteContribution(contributionId)

        // Sync deletion to cloud in background
        syncScope.launch {
            try {
                cloudContributionDataSource.deleteContribution(groupId, contributionId)
                Timber.d("Contribution deletion synced to cloud: $contributionId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync contribution deletion to cloud, will retry later")
            }
        }
    }

    override suspend fun deleteByLinkedExpenseId(groupId: String, linkedExpenseId: String) {
        // Find locally first to get the contribution ID for cloud deletion
        val linkedContribution = localContributionDataSource.findByLinkedExpenseId(linkedExpenseId)

        // Delete from local first - UI updates instantly via Flow
        localContributionDataSource.deleteByLinkedExpenseId(linkedExpenseId)

        // Sync deletion to cloud in background (only if we found a contribution to delete)
        linkedContribution?.let { contribution ->
            syncScope.launch {
                try {
                    cloudContributionDataSource.deleteContribution(groupId, contribution.id)
                    Timber.d("Linked contribution deletion synced to cloud: ${contribution.id}")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to sync linked contribution deletion to cloud")
                }
            }
        }
    }

    override suspend fun findByLinkedExpenseId(
        groupId: String,
        linkedExpenseId: String
    ): Contribution? = localContributionDataSource.findByLinkedExpenseId(linkedExpenseId)

    /**
     * Subscribes to real-time Firestore snapshot changes for a group's contributions.
     *
     * The Firestore snapshotListener fires whenever ANY user adds, modifies, or
     * deletes a contribution in this group. Each snapshot represents the complete
     * authoritative state of the collection.
     *
     * We use [replaceContributionsForGroup] with a merge reconciliation strategy
     * (upsert remote + selective delete of stale) to safely reconcile the
     * local DB with the cloud snapshot.
     */
    private suspend fun subscribeToCloudChanges(groupId: String) {
        try {
            cloudContributionDataSource.getContributionsByGroupIdFlow(groupId)
                .collect { remoteContributions ->
                    try {
                        Timber.d("Real-time sync: ${remoteContributions.size} contributions for group $groupId")
                        localContributionDataSource.replaceContributionsForGroup(
                            groupId,
                            remoteContributions
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling contributions from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud contribution changes, using local cache")
        }
    }
}
