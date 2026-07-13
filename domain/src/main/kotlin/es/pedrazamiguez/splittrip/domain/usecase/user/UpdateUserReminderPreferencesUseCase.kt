package es.pedrazamiguez.splittrip.domain.usecase.user

import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface UpdateUserReminderPreferencesUseCase : UseCase {
    suspend operator fun invoke(userId: String, timezone: String?, preferredReminderTime: String?): Result<Unit>
}
