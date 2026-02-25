package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface CloudGroupDataSource {
    suspend fun createGroup(group: Group): String
    suspend fun getGroupById(groupId: String): Group?
    suspend fun deleteGroup(groupId: String)

    /**
     * One-shot fetch of all groups for sync purposes.
     * Backed by a Firestore .get().await() call that uses the default source
     * (server when available, but may fall back to the local cache).
     * Exceptions propagate to the caller; use this for background sync operations
     * instead of the reactive Flow.
     */
    suspend fun fetchAllGroups(): List<Group>

    /**
     * Reactive stream of groups for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getAllGroupsFlow(): Flow<List<Group>>
}
