package es.pedrazamiguez.expenseshareapp.domain.usecase.subunit

import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService

/**
 * Use case for deleting a sub-unit from a group.
 *
 * Enforces membership validation before deletion.
 */
class DeleteSubunitUseCase(
    private val subunitRepository: SubunitRepository,
    private val groupMembershipService: GroupMembershipService
) {

    /**
     * Deletes a sub-unit by its ID within a group.
     *
     * @param groupId The ID of the group containing the sub-unit.
     * @param subunitId The ID of the sub-unit to delete.
     * @throws NotGroupMemberException if the user is not a member of the group.
     */
    suspend operator fun invoke(groupId: String, subunitId: String) {
        groupMembershipService.requireMembership(groupId)
        subunitRepository.deleteSubunit(groupId, subunitId)
    }
}

