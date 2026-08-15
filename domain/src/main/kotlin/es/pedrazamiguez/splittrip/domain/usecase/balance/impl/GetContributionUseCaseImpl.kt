package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetContributionUseCase

class GetContributionUseCaseImpl(
    private val contributionRepository: ContributionRepository
) : GetContributionUseCase {
    override suspend fun invoke(contributionId: String): Contribution? =
        contributionRepository.getContribution(contributionId)
}
