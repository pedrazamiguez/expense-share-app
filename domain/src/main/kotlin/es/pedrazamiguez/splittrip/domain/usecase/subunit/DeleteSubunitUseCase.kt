package es.pedrazamiguez.splittrip.domain.usecase.subunit

import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService

/**
 * Use case for deleting a subunit from a group.
 *
 * Enforces membership validation before deletion.
 */
class DeleteSubunitUseCase(
    private val subunitRepository: SubunitRepository,
    private val groupMembershipService: GroupMembershipService
) {

    /**
     * Deletes a subunit by its ID within a group.
     *
     * @param groupId The ID of the group containing the subunit.
     * @param subunitId The ID of the subunit to delete.
     * @throws NotGroupMemberException if the user is not a member of the group.
     */
    suspend operator fun invoke(groupId: String, subunitId: String) {
        groupMembershipService.requireMembership(groupId)
        subunitRepository.deleteSubunit(groupId, subunitId)
    }
}
