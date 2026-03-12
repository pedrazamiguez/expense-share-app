package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationCategory
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationPreferencesRepository

class UpdateNotificationPreferenceUseCase(
    private val repository: NotificationPreferencesRepository
) {
    suspend operator fun invoke(category: NotificationCategory, enabled: Boolean) {
        repository.updatePreference(category, enabled)
    }
}

