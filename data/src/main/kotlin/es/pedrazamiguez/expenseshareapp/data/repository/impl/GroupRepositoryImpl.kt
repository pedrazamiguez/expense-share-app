package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Offline-First implementation of GroupRepository.
 *
 * Pattern: Single Source of Truth
 * - The UI ALWAYS reads from the local database (Room)
 * - The repository syncs with the cloud in the background
 * - If sync fails (no internet), the user still sees local data
 */
class GroupRepositoryImpl(
    private val cloudGroupDataSource: CloudGroupDataSource,
    private val localGroupDataSource: LocalGroupDataSource,
    private val authenticationService: AuthenticationService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GroupRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

    /**
     * Tracks the single active cloud subscription Job for groups.
     * Prevents duplicate Firestore snapshot listeners from accumulating
     * when onStart fires multiple times (e.g., config changes, tab switches,
     * WhileSubscribed resubscriptions).
     */
    private var cloudSubscriptionJob: Job? = null

    /**
     * Returns a Flow of groups from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     * This is INSTANT because data comes from Room.
     *
     * Uses a single shared subscription: any existing cloud listener is cancelled
     * before starting a new one, preventing duplicate snapshot listeners.
     */
    override fun getAllGroupsFlow(): Flow<List<Group>> = localGroupDataSource.getGroupsFlow()
        .onStart {
            // Cancel any previous cloud subscription to prevent duplicates.
            // This can fire multiple times due to WhileSubscribed resubscription,
            // config changes, or tab switches.
            cloudSubscriptionJob?.cancel()
            cloudSubscriptionJob = syncScope.launch {
                subscribeToCloudChanges()
            }
        }

    /**
     * Gets a group by ID.
     * First tries local, then falls back to cloud if not found locally.
     */
    override suspend fun getGroupById(groupId: String): Group? {
        // Try local first (instant)
        localGroupDataSource.getGroupById(groupId)?.let { return it }

        // If not found locally, try cloud and cache it
        return try {
            cloudGroupDataSource.getGroupById(groupId)?.also { group ->
                localGroupDataSource.saveGroup(group)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching group from cloud: $groupId")
            null
        }
    }

    /**
     * Creates a group locally first, then syncs to cloud.
     * Ensures offline support by saving to local database before cloud sync.
     */
    override suspend fun createGroup(group: Group): String {
        val groupId = java.util.UUID.randomUUID().toString()
        val currentTimestamp = java.time.LocalDateTime.now()
        val currentUserId = authenticationService.requireUserId()

        // Ensure the creator is always in the members list.
        // This is enforced here (repository layer) so it applies to all callers
        // and the locally-saved Group is consistent with what Firestore will have.
        val membersWithCreator = if (currentUserId !in group.members) {
            group.members + currentUserId
        } else {
            group.members
        }

        val createdGroup = group.copy(
            id = groupId,
            members = membersWithCreator,
            createdAt = group.createdAt ?: currentTimestamp,
            lastUpdatedAt = currentTimestamp
        )

        // Save to local FIRST - UI updates instantly
        localGroupDataSource.saveGroup(createdGroup)

        // Sync to cloud in background
        syncScope.launch {
            try {
                cloudGroupDataSource.createGroup(createdGroup)
                Timber.d("Group synced to cloud: $groupId")
            } catch (e: Exception) {
                Timber.w(e, "Failed to sync group to cloud, will retry later")
            }
        }

        return groupId
    }

    /**
     * Deletes a group using a server-side cascading delete strategy.
     *
     * This approach delegates the cascading subcollection cleanup to the
     * `onGroupDeletionRequested` Cloud Function, solving the fundamental problems
     * of the previous client-side "Capture-then-Kill" strategy:
     * - Incomplete ID capture (client only had locally-synced subset)
     * - Non-atomic sequential deletes that could fail midway
     * - Notification spam from per-entity Cloud Function triggers
     * - Stale data on other devices due to late member removal
     *
     * Flow:
     * 1. Delete group from Room — FK CASCADE handles all child entities locally.
     *    UI updates instantly.
     * 2. Signal Firestore via `requestGroupDeletion()` which sets
     *    `deletionRequested = true` on the group document. This triggers the
     *    Cloud Function to:
     *    a. Delete members subcollection (fires snapshot listener on other devices)
     *    b. Delete all other subcollections in parallel (with notification suppression)
     *    c. Send a single GROUP_DELETED notification
     *    d. Delete the group document
     */
    override suspend fun deleteGroup(groupId: String) {
        // 1. Delete from Room immediately — FK CASCADE handles child entities.
        // UI updates instantly via the observed Room Flow.
        localGroupDataSource.deleteGroup(groupId)

        // 2. Signal Firestore to initiate server-side cascading delete.
        syncScope.launch {
            try {
                cloudGroupDataSource.requestGroupDeletion(groupId)
                Timber.d("Group deletion requested for cloud: $groupId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to request cloud deletion for group: $groupId")
                // TODO: Enqueue for WorkManager retry if offline
            }
        }
    }

    /**
     * Subscribes to real-time Firestore snapshot changes for the user's groups.
     *
     * The Firestore snapshotListener fires whenever ANY change occurs to the
     * user's group membership (groups added, removed, or modified by any user).
     * Each snapshot represents the complete authoritative set of groups.
     *
     * We use [replaceAllGroups] with a merge reconciliation strategy
     * (upsert remote + selective delete of stale) to safely reconcile the
     * local DB with the cloud snapshot. This handles:
     * - Groups created by other users that include the current user
     * - Groups deleted by other users → stale groups are removed locally
     * - Group modifications by other users → groups are updated locally
     * - Locally-created groups not yet synced → preserved (not deleted)
     *
     * The Room Flow re-emits automatically after each reconciliation,
     * keeping the UI in sync across all devices in near real-time.
     */
    private suspend fun subscribeToCloudChanges() {
        try {
            cloudGroupDataSource.getAllGroupsFlow()
                .collect { remoteGroups ->
                    try {
                        Timber.d("Real-time sync: ${remoteGroups.size} groups received")
                        localGroupDataSource.replaceAllGroups(remoteGroups)
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling groups from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud group changes, using local cache")
        }
    }
}
