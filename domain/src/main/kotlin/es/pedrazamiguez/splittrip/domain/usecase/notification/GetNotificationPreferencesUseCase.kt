package es.pedrazamiguez.splittrip.domain.usecase.notification

import es.pedrazamiguez.splittrip.domain.model.NotificationPreferences
import es.pedrazamiguez.splittrip.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationPreferencesUseCase(private val repository: NotificationPreferencesRepository) {
    operator fun invoke(): Flow<NotificationPreferences> = repository.getPreferencesFlow()
}
