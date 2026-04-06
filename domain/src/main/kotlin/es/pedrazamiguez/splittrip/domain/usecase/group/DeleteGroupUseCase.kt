package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.repository.GroupRepository

/**
 * Use case for deleting a group.
 *
 * This encapsulates the business logic for group deletion.
 * Future enhancements could include:
 * - Permission checks (e.g., "Only Admins can delete")
 * - Pre-deletion validation
 * - Audit logging
 */
class DeleteGroupUseCase(private val groupRepository: GroupRepository) {
    /**
     * Deletes a group by its ID.
     *
     * The repository deletes the group from the local database immediately
     * (Room FK CASCADE handles child entities), then signals Firestore to
     * initiate a server-side cascading delete via the `onGroupDeletionRequested`
     * Cloud Function.
     *
     * @param groupId The ID of the group to delete.
     */
    suspend operator fun invoke(groupId: String) {
        groupRepository.deleteGroup(groupId)
    }
}
