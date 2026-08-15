package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService
import es.pedrazamiguez.splittrip.domain.usecase.balance.UpdateContributionUseCase

class UpdateContributionUseCaseImpl(
    private val contributionRepository: ContributionRepository,
    private val groupMembershipService: GroupMembershipService
) : UpdateContributionUseCase {
    override suspend fun invoke(groupId: String, contribution: Contribution) {
        groupMembershipService.requireMembership(groupId)
        contributionRepository.updateContribution(groupId, contribution)
    }
}
