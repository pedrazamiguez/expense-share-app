package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for Group entities.
 * This is part of the Offline-First architecture where Room serves
 * as the Single Source of Truth for the UI.
 */
interface LocalGroupDataSource {

    /**
     * Observes all groups from the local database.
     * This Flow emits whenever the groups table changes.
     */
    fun getGroupsFlow(): Flow<List<Group>>

    /**
     * Gets a single group by ID from local storage.
     */
    suspend fun getGroupById(groupId: String): Group?

    /**
     * Observes a single group by ID.
     */
    fun getGroupByIdFlow(groupId: String): Flow<Group?>

    /**
     * Saves groups to local storage.
     * Existing groups with the same ID will be replaced.
     */
    suspend fun saveGroups(groups: List<Group>)

    /**
     * Saves a single group to local storage.
     */
    suspend fun saveGroup(group: Group)

    /**
     * Replaces all groups atomically.
     * Useful for full sync operations.
     */
    suspend fun replaceAllGroups(groups: List<Group>)

    /**
     * Deletes a group from local storage.
     */
    suspend fun deleteGroup(groupId: String)

    /**
     * Clears all groups from local storage.
     */
    suspend fun clearAllGroups()
}
