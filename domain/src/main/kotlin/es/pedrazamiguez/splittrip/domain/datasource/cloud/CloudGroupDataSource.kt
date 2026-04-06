package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface CloudGroupDataSource {
    suspend fun createGroup(group: Group): String
    suspend fun getGroupById(groupId: String): Group?
    suspend fun deleteGroup(groupId: String)

    /**
     * Signals Firestore to initiate a server-side cascading group deletion.
     *
     * This sets `deletionRequested = true` on the group document, which triggers
     * the `onGroupDeletionRequested` Cloud Function to delete all subcollections
     * (members, expenses, contributions, cash_withdrawals, subunits) and finally
     * the group document itself — server-side with best-effort retries,
     * avoiding notification spam from individual subcollection deletions.
     *
     * The operation is idempotent: calling this on a group that already has
     * `deletionRequested = true` is a safe no-op for the Cloud Function trigger
     * (its guard condition prevents re-execution).
     *
     * @param groupId The ID of the group to request deletion for.
     * @throws Exception if the Firestore update fails (e.g. offline). The caller
     *   should schedule a retry via [GroupDeletionRetryScheduler].
     */
    suspend fun requestGroupDeletion(groupId: String)

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
