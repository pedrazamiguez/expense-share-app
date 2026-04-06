package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import kotlinx.coroutines.flow.Flow

class GetGroupContributionsFlowUseCase(private val contributionRepository: ContributionRepository) {
    operator fun invoke(groupId: String): Flow<List<Contribution>> =
        contributionRepository.getGroupContributionsFlow(groupId)
}
