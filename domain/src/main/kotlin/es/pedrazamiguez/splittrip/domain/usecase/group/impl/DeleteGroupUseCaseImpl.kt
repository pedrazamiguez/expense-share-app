package es.pedrazamiguez.splittrip.domain.usecase.group.impl

import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreGroupSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.DeleteGroupUseCase

class DeleteGroupUseCaseImpl(
    private val groupRepository: GroupRepository,
    private val areGroupSettlementsResolvedUseCase: AreGroupSettlementsResolvedUseCase
) : DeleteGroupUseCase {

    /**
     * Deletes a group by its ID.
     *
     * The repository deletes the group from the local database immediately
     * (Room FK CASCADE handles child entities), then signals Firestore to
     * initiate a server-side cascading delete via the `onGroupDeletionRequested`
     * Cloud Function.
     *
     * @param groupId The ID of the group to delete.
     * @throws UnresolvedSettlementsException if the group has unresolved settlements.
     */
    override suspend operator fun invoke(groupId: String) {
        requireNotNull(groupRepository.getGroupById(groupId)) {
            "Group not found with id: $groupId"
        }
        val unresolvedSettlements = areGroupSettlementsResolvedUseCase(groupId)
        if (unresolvedSettlements.isNotEmpty()) {
            throw UnresolvedSettlementsException(groupId, unresolvedSettlements)
        }
        groupRepository.deleteGroup(groupId)
    }
}
