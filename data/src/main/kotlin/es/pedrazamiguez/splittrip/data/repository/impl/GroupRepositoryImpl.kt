package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.worker.GroupDeletionRetryScheduler
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import java.util.concurrent.ConcurrentHashMap
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
    private val groupDeletionRetryScheduler: GroupDeletionRetryScheduler,
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
     * Tracks IDs of groups deleted locally while in PENDING_SYNC state (never synced to server).
     * Prevents the Firestore snapshot listener's pending write cache from resurrecting
     * these entities during reconciliation. Safe as in-memory only: if the process dies,
     * the Firestore SDK's pending write cache also dies, eliminating the resurrection vector.
     */
    private val deletedPendingSyncIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

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
     *
     * The sync status transitions follow a two-phase verification:
     * 1. Cloud write: batch commit to Firestore (may resolve from local cache if offline)
     * 2. Server verification: Source.SERVER read to confirm the write reached the server
     *
     * - Online: both succeed → SYNCED
     * - Offline: write cached locally, verification fails → stays PENDING_SYNC
     * - Error: write rejected by Firestore (permissions, etc.) → SYNC_FAILED
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
            lastUpdatedAt = currentTimestamp,
            syncStatus = SyncStatus.PENDING_SYNC
        )

        // Save to local FIRST - UI updates instantly
        localGroupDataSource.saveGroup(createdGroup)

        // Sync to cloud in background with two-phase verification
        syncScope.launch {
            // Phase 1: Write to Firestore (resolves from local cache if offline)
            try {
                cloudGroupDataSource.createGroup(createdGroup)
            } catch (e: Exception) {
                // Actual write failure (permission denied, invalid data, etc.)
                localGroupDataSource.updateSyncStatus(groupId, SyncStatus.SYNC_FAILED)
                Timber.w(e, "Failed to sync group to cloud")
                return@launch
            }

            // Phase 2: Verify the write reached the server (Source.SERVER round-trip)
            try {
                cloudGroupDataSource.verifyGroupOnServer(groupId)
                localGroupDataSource.updateSyncStatus(groupId, SyncStatus.SYNCED)
                Timber.d("Group synced and confirmed on server: $groupId")
            } catch (e: Exception) {
                // Server unreachable — write is cached in Firestore, will sync automatically.
                // Keep as PENDING_SYNC (already the current status from local save).
                // The confirmPendingSyncGroups() mechanism will transition to SYNCED
                // when the snapshot listener re-fires after server confirmation.
                Timber.d("Group saved to Firestore cache, pending server confirmation: $groupId")
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
        // Check sync status before deleting — if PENDING_SYNC, the group was never
        // synced to the server, so there's nothing to delete remotely.
        val group = localGroupDataSource.getGroupById(groupId)
        val wasPendingSync = group?.syncStatus == SyncStatus.PENDING_SYNC

        // 1. Delete from Room immediately — FK CASCADE handles child entities.
        // UI updates instantly via the observed Room Flow.
        localGroupDataSource.deleteGroup(groupId)

        if (wasPendingSync) {
            // Track the ID to prevent resurrection via snapshot reconciliation.
            // The Firestore SDK's pending write cache from createGroup() may still
            // include this group — the snapshot listener would re-insert it without
            // this protection.
            deletedPendingSyncIds.add(groupId)
            Timber.d("Group deleted locally (was PENDING_SYNC, skipping cloud): $groupId")
            return
        }

        // 2. Signal Firestore to initiate server-side cascading delete.
        syncScope.launch {
            try {
                cloudGroupDataSource.requestGroupDeletion(groupId)
                Timber.d("Group deletion requested for cloud: $groupId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to request cloud deletion for group: $groupId")
                groupDeletionRetryScheduler.scheduleRetry(groupId)
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
     * After reconciliation, [confirmPendingSyncGroups] attempts to verify
     * any PENDING_SYNC items against the server. This handles the
     * PENDING_SYNC → SYNCED transition when the device comes back online
     * and the snapshot listener re-fires with MetadataChanges.INCLUDE.
     *
     * The Room Flow re-emits automatically after each reconciliation,
     * keeping the UI in sync across all devices in near real-time.
     */
    private suspend fun subscribeToCloudChanges() {
        try {
            cloudGroupDataSource.getAllGroupsFlow()
                .collect { remoteGroups ->
                    try {
                        // Filter out groups that were deleted locally while PENDING_SYNC.
                        // These may appear in the snapshot due to the Firestore SDK's
                        // pending write cache from the original createGroup() call.
                        val filtered = if (deletedPendingSyncIds.isNotEmpty()) {
                            remoteGroups.filter { it.id !in deletedPendingSyncIds }
                        } else {
                            remoteGroups
                        }

                        val filteredCount = remoteGroups.size - filtered.size
                        Timber.d(
                            "Real-time sync: %d groups received (%d filtered)",
                            filtered.size,
                            filteredCount
                        )
                        localGroupDataSource.replaceAllGroups(filtered)
                        confirmPendingSyncGroups()

                        // Clean up: IDs from this snapshot cycle have been excluded
                        if (deletedPendingSyncIds.isNotEmpty()) {
                            val remoteIds = remoteGroups.map { it.id }.toSet()
                            deletedPendingSyncIds.removeAll(remoteIds)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling groups from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud group changes, using local cache")
        }
    }

    /**
     * Attempts to confirm PENDING_SYNC groups by verifying their existence on the server.
     *
     * Called after each reconciliation cycle. When the device is online and Firestore
     * has confirmed the pending write, the server verification succeeds and the
     * group transitions to SYNCED. When offline, the verification throws and the
     * group remains PENDING_SYNC.
     *
     * This mechanism works in conjunction with MetadataChanges.INCLUDE on the
     * Firestore snapshot listener, which ensures the listener re-fires when
     * pending writes are confirmed by the server — triggering a new reconciliation
     * cycle that reaches this method.
     */
    private suspend fun confirmPendingSyncGroups() {
        val pendingIds = localGroupDataSource.getPendingSyncGroupIds()
        if (pendingIds.isEmpty()) return

        for (id in pendingIds) {
            try {
                if (cloudGroupDataSource.verifyGroupOnServer(id)) {
                    localGroupDataSource.updateSyncStatus(id, SyncStatus.SYNCED)
                    Timber.d("Confirmed group sync: $id")
                }
            } catch (e: Exception) {
                // Server unreachable — keep as PENDING_SYNC
                Timber.d("Cannot confirm group $id — server unreachable")
            }
        }
    }
}
