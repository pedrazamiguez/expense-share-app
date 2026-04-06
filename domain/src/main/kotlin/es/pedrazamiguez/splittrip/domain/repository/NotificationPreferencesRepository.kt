package es.pedrazamiguez.splittrip.domain.repository

import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import es.pedrazamiguez.splittrip.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationPreferencesRepository {
    fun getPreferencesFlow(): Flow<NotificationPreferences>
    suspend fun updatePreference(category: NotificationCategory, enabled: Boolean)
}
