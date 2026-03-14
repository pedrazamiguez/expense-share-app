package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import kotlinx.coroutines.flow.Flow

class GetGroupContributionsFlowUseCase(private val contributionRepository: ContributionRepository) {
    operator fun invoke(groupId: String): Flow<List<Contribution>> =
        contributionRepository.getGroupContributionsFlow(groupId)
}
