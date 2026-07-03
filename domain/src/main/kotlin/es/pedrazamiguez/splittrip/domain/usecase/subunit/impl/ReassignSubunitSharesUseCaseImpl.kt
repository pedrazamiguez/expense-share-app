package es.pedrazamiguez.splittrip.domain.usecase.subunit.impl

import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.SubunitShareDistributionService
import es.pedrazamiguez.splittrip.domain.usecase.subunit.ReassignSubunitSharesUseCase

class ReassignSubunitSharesUseCaseImpl(
    private val subunitRepository: SubunitRepository,
    private val subunitShareDistributionService: SubunitShareDistributionService
) : ReassignSubunitSharesUseCase {

    override suspend operator fun invoke(groupId: String, leavingUserId: String): Result<Unit> = runCatching {
        val subunits = subunitRepository.getGroupSubunits(groupId)

        subunits.forEach { subunit ->
            if (leavingUserId !in subunit.memberIds) return@forEach

            val updatedMemberIds = subunit.memberIds.filter { it != leavingUserId }

            val updatedMemberShares = if (leavingUserId in subunit.memberShares) {
                subunitShareDistributionService.rescaleSharesAfterRemoval(
                    removedMemberId = leavingUserId,
                    currentShares = subunit.memberShares
                )
            } else {
                subunit.memberShares
            }

            subunitRepository.updateSubunit(
                groupId = groupId,
                subunit = subunit.copy(
                    memberIds = updatedMemberIds,
                    memberShares = updatedMemberShares
                )
            )
        }
    }
}
