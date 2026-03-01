package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
    private val cloudExpenseDataSource: CloudExpenseDataSource,
    private val localExpenseDataSource: LocalExpenseDataSource,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GroupRepository {

    private val syncScope = CoroutineScope(ioDispatcher)

    /**
     * Returns a Flow of groups from local storage.
     * On start, subscribes to real-time cloud changes for multi-user sync.
     * This is INSTANT because data comes from Room.
     *
     * Uses channelFlow so that the cloud subscription lifecycle is tied to
     * the flow's collection — when the collector cancels, both the local
     * observation and the cloud listener are cancelled together, preventing
     * orphan snapshot listeners or duplicate reconciliation loops.
     */
    override fun getAllGroupsFlow(): Flow<List<Group>> = channelFlow {
        // Forward local Room data to downstream collectors.
        launch {
            try {
                localGroupDataSource.getGroupsFlow().collect { send(it) }
            } catch (e: Exception) {
                Timber.e(e, "Error reading local groups")
            }
        }

        // Cloud subscription - lifecycle is tied to this flow's collection.
        try {
            cloudGroupDataSource.getAllGroupsFlow()
                .collect { remoteGroups ->
                    try {
                        Timber.d("Real-time sync: ${remoteGroups.size} groups received")
                        // Incremental upsert: preserves locally-pending groups that
                        // haven't synced yet (e.g., created offline) instead of
                        // wiping the entire local table.
                        localGroupDataSource.saveGroups(remoteGroups)
                    } catch (e: Exception) {
                        Timber.w(e, "Error reconciling groups from cloud snapshot")
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Error subscribing to cloud group changes, using local cache")
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

        val createdGroup = group.copy(
            id = groupId,
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
     * Deletes a group using the "Capture-then-Kill" strategy.
     *
     * This approach is critical for Offline-First architecture with Firestore because:
     * 1. Firestore doesn't support automatic cascading deletes for subcollections
     * 2. Once we delete the local Group, the foreign key cascade wipes local expenses
     * 3. We need the expense IDs to clean up Firestore later
     *
     * Flow:
     * 1. CAPTURE: Get expense IDs before deletion
     * 2. KILL (Local): Delete group from Room (cascades to expenses locally)
     * 3. KILL (Remote): Background sync deletes from Firestore
     */
    override suspend fun deleteGroup(groupId: String) {
        // 1. CAPTURE: Retrieve IDs of expenses associated with the group.
        // Critical for Offline-First: We need these IDs to clean up Firestore later,
        // because once we delete the local Group, any associated local expenses will be
        // removed, making them impossible to query.
        val expenseIdsToDelete = localExpenseDataSource.getExpenseIdsByGroup(groupId)

        // 2.1 KILL (Local Expenses): Explicitly delete expenses to avoid orphans in Room.
        // While the foreign key CASCADE would handle this, we do it explicitly for clarity
        // and to ensure compatibility if the foreign key constraint is ever removed.
        localExpenseDataSource.deleteExpensesByGroupId(groupId)

        // 2.2 KILL (Local Group): Delete the group from Room.
        localGroupDataSource.deleteGroup(groupId)

        // 3. KILL (Remote): Background Sync
        // We use the 'captured' IDs to perform the cleanup in the cloud.
        syncScope.launch {
            try {
                // A. Delete sub-collection documents (Expenses)
                expenseIdsToDelete.forEach { expenseId ->
                    cloudExpenseDataSource.deleteExpense(groupId, expenseId)
                }

                // B. Delete the parent document (Group)
                cloudGroupDataSource.deleteGroup(groupId)

                Timber.d("Sync Delete Complete: Group $groupId and ${expenseIdsToDelete.size} expenses.")
            } catch (e: Exception) {
                Timber.e(e, "Sync Delete Failed for group: $groupId")
                // TODO: Enqueue for WorkManager retry if offline
            }
        }
    }
}
