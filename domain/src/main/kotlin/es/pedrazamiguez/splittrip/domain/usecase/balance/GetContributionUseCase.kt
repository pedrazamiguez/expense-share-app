package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.Contribution

interface GetContributionUseCase {
    suspend operator fun invoke(contributionId: String): Contribution?
}
