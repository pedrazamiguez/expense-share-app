package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository

class AddContributionUseCase(
    private val contributionRepository: ContributionRepository
) {
    suspend operator fun invoke(groupId: String, contribution: Contribution) {
        contributionRepository.addContribution(groupId, contribution)
    }
}

