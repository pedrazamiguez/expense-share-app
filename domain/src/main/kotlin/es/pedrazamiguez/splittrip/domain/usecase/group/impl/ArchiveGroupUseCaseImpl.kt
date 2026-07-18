package es.pedrazamiguez.splittrip.domain.usecase.group.impl

import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.exception.CannotArchiveGroupException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreGroupSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ArchiveGroupUseCase
import java.time.LocalDateTime
import java.util.UUID

class ArchiveGroupUseCaseImpl(
    private val groupRepository: GroupRepository,
    private val authenticationService: AuthenticationService,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val areGroupSettlementsResolvedUseCase: AreGroupSettlementsResolvedUseCase
) : ArchiveGroupUseCase {

    override suspend operator fun invoke(groupId: String): Result<Unit> = runCatching {
        val group = groupRepository.getGroupByIdLocal(groupId)
            ?: throw IllegalArgumentException("Group not found with id: $groupId")

        val currentUserId = authenticationService.requireUserId()
        if (group.createdBy != currentUserId) {
            throw CannotArchiveGroupException()
        }

        getSettlementSuggestionsUseCase.persistForGroup(groupId)

        val unresolvedSettlements = areGroupSettlementsResolvedUseCase(groupId)
        if (unresolvedSettlements.isNotEmpty()) {
            throw UnresolvedSettlementsException(groupId, unresolvedSettlements)
        }

        val updatedGroup = group.copy(
            status = GroupStatus.ARCHIVED,
            lastUpdatedAt = LocalDateTime.now(),
            lastArchiveEventId = UUID.randomUUID().toString()
        )
        groupRepository.updateGroup(updatedGroup)
    }
}
