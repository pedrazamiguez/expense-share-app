package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.Contribution

interface UpdateContributionUseCase {
    suspend operator fun invoke(groupId: String, contribution: Contribution)
}
