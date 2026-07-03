package es.pedrazamiguez.splittrip.domain.usecase.group.impl

import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreGroupSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ArchiveGroupUseCase
import java.time.LocalDateTime

class ArchiveGroupUseCaseImpl(
    private val groupRepository: GroupRepository,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val areGroupSettlementsResolvedUseCase: AreGroupSettlementsResolvedUseCase
) : ArchiveGroupUseCase {

    override suspend operator fun invoke(groupId: String): Result<Unit> = runCatching {
        val group = groupRepository.getGroupById(groupId)
            ?: throw IllegalArgumentException("Group not found with id: $groupId")

        getSettlementSuggestionsUseCase.persistForGroup(groupId)

        val unresolvedSettlements = areGroupSettlementsResolvedUseCase(groupId)
        if (unresolvedSettlements.isNotEmpty()) {
            throw UnresolvedSettlementsException(groupId, unresolvedSettlements)
        }

        val updatedGroup = group.copy(
            status = GroupStatus.ARCHIVED,
            lastUpdatedAt = LocalDateTime.now()
        )
        groupRepository.updateGroup(updatedGroup)
    }
}
