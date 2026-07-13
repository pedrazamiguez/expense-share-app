package es.pedrazamiguez.splittrip.domain.usecase.user.impl

import es.pedrazamiguez.splittrip.domain.repository.UserRepository
import es.pedrazamiguez.splittrip.domain.usecase.user.UpdateUserReminderPreferencesUseCase

class UpdateUserReminderPreferencesUseCaseImpl(
    private val userRepository: UserRepository
) : UpdateUserReminderPreferencesUseCase {
    override suspend operator fun invoke(
        userId: String,
        timezone: String?,
        preferredReminderTime: String?
    ): Result<Unit> {
        return userRepository.updateUserReminderPreferences(userId, timezone, preferredReminderTime)
    }
}
