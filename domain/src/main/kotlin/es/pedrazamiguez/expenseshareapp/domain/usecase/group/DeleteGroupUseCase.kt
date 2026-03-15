package es.pedrazamiguez.expenseshareapp.domain.usecase.group

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

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
     * The repository handles the "Capture-then-Kill" synchronization strategy
     * to ensure proper cleanup of associated expenses in both local and cloud storage.
     *
     * @param groupId The ID of the group to delete.
     */
    suspend operator fun invoke(groupId: String) {
        groupRepository.deleteGroup(groupId)
    }
}
