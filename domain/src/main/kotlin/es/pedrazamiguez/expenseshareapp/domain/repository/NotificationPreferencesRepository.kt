package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationCategory
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationPreferencesRepository {
    fun getPreferencesFlow(): Flow<NotificationPreferences>
    suspend fun updatePreference(category: NotificationCategory, enabled: Boolean)
}
