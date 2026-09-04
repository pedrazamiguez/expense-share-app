package es.pedrazamiguez.splittrip.domain.usecase.user

import es.pedrazamiguez.splittrip.domain.enums.SubscriptionTier
import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface UpdateUserTierUseCase : UseCase {
    suspend operator fun invoke(userId: String, tier: SubscriptionTier): Result<Unit>
}
