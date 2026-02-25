package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface CloudGroupDataSource {
    suspend fun createGroup(group: Group): String
    suspend fun getGroupById(groupId: String): Group?
    suspend fun deleteGroup(groupId: String)

    /**
     * One-shot fetch of all groups from the server for sync purposes.
     * Uses .get().await() to wait for the actual server response.
     * Use this for background sync operations instead of the reactive Flow.
     */
    suspend fun fetchAllGroups(): List<Group>

    /**
     * Reactive stream of groups for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getAllGroupsFlow(): Flow<List<Group>>
}
