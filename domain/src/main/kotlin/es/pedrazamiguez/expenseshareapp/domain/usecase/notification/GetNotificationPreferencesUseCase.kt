package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.model.NotificationPreferences
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationPreferencesUseCase(
    private val repository: NotificationPreferencesRepository
) {
    operator fun invoke(): Flow<NotificationPreferences> = repository.getPreferencesFlow()
}

