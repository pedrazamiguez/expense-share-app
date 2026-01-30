package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val localGroupDataSource: LocalGroupDataSource
) : GroupRepository {

    private val syncScope = CoroutineScope(Dispatchers.IO)

    /**
     * Returns a Flow of groups from local storage.
     * On start, triggers a background sync with the cloud.
     * This is INSTANT because data comes from Room.
     */
    override fun getAllGroupsFlow(): Flow<List<Group>> {
        return localGroupDataSource.getGroupsFlow()
            .onStart {
                // Trigger background sync when someone starts observing
                syncScope.launch {
                    refreshGroupsFromCloud()
                }
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
     * Creates a group in the cloud and saves it locally.
     * The local save triggers the Flow to emit, updating the UI.
     */
    override suspend fun createGroup(group: Group): String {
        val groupId = cloudGroupDataSource.createGroup(group)

        // Save to local immediately so UI updates
        val createdGroup = group.copy(id = groupId)
        localGroupDataSource.saveGroup(createdGroup)

        return groupId
    }

    /**
     * Syncs groups from cloud to local storage.
     * If this fails (e.g., no internet), we silently continue with local data.
     */
    private suspend fun refreshGroupsFromCloud() {
        try {
            Timber.d("Starting groups sync from cloud...")

            // Get the first emission from cloud and save to local
            // Using first() ensures we only get the initial snapshot and terminate
            val remoteGroups = cloudGroupDataSource.getAllGroupsFlow().first()
            Timber.d("Received ${remoteGroups.size} groups from cloud, saving to local")
            localGroupDataSource.saveGroups(remoteGroups)
        } catch (e: Exception) {
            // Offline or error - no problem, we use cached local data
            Timber.w(e, "Error syncing groups from cloud, using local cache")
        }
    }
}
