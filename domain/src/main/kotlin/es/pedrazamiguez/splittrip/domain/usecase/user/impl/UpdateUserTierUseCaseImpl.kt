package es.pedrazamiguez.splittrip.domain.usecase.user.impl

import es.pedrazamiguez.splittrip.domain.enums.SubscriptionTier
import es.pedrazamiguez.splittrip.domain.repository.UserRepository
import es.pedrazamiguez.splittrip.domain.usecase.user.UpdateUserTierUseCase

class UpdateUserTierUseCaseImpl(
    private val userRepository: UserRepository
) : UpdateUserTierUseCase {
    override suspend operator fun invoke(
        userId: String,
        tier: SubscriptionTier
    ): Result<Unit> {
        return userRepository.updateUserTier(userId, tier)
    }
}
