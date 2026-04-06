package es.pedrazamiguez.splittrip.domain.usecase.notification

import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import es.pedrazamiguez.splittrip.domain.repository.NotificationPreferencesRepository

class UpdateNotificationPreferenceUseCase(private val repository: NotificationPreferencesRepository) {
    suspend operator fun invoke(category: NotificationCategory, enabled: Boolean) {
        repository.updatePreference(category, enabled)
    }
}
