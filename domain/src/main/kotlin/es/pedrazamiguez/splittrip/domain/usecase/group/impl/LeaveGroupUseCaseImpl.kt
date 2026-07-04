package es.pedrazamiguez.splittrip.domain.usecase.group.impl

import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.GroupArchivedException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.LeaveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.ReassignSubunitSharesUseCase

class LeaveGroupUseCaseImpl(
    private val groupRepository: GroupRepository,
    private val authenticationService: AuthenticationService,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val areMemberSettlementsResolvedUseCase: AreMemberSettlementsResolvedUseCase,
    private val reassignSubunitSharesUseCase: ReassignSubunitSharesUseCase
) : LeaveGroupUseCase {

    override suspend operator fun invoke(groupId: String): Result<Unit> = runCatching {
        val currentUserId = authenticationService.requireUserId()
        val group = groupRepository.getGroupById(groupId)
            ?: throw IllegalArgumentException("Group not found: $groupId")

        if (group.status.name == "ARCHIVED") throw GroupArchivedException(groupId)
        if (currentUserId !in group.members) throw CannotLeaveGroupException("not_a_member")
        if (group.createdBy == currentUserId) throw CannotLeaveGroupException("is_creator")

        getSettlementSuggestionsUseCase.persistForGroup(groupId)

        val unresolvedSettlements = areMemberSettlementsResolvedUseCase(groupId, currentUserId)
        if (unresolvedSettlements.isNotEmpty()) {
            throw UnresolvedSettlementsException(groupId, unresolvedSettlements)
        }

        reassignSubunitSharesUseCase(groupId, currentUserId).getOrThrow()
        groupRepository.leaveGroup(groupId)
    }
}
