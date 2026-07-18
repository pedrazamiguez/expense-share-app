package es.pedrazamiguez.splittrip.features.settings.presentation.mapper

import es.pedrazamiguez.splittrip.domain.model.NotificationPreferences
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState

interface NotificationPreferencesUiMapper {
    fun toUiState(
        prefs: NotificationPreferences,
        user: User?
    ): NotificationPreferencesUiState

    fun formatTime(hour: Int, minute: Int): String
}
