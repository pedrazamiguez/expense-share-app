package es.pedrazamiguez.splittrip.features.settings.presentation.mapper.impl

import es.pedrazamiguez.splittrip.domain.model.NotificationPreferences
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.NotificationPreferencesUiMapper
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState
import java.time.ZoneId
import java.util.Locale

class NotificationPreferencesUiMapperImpl : NotificationPreferencesUiMapper {
    override fun toUiState(
        prefs: NotificationPreferences,
        user: User?
    ): NotificationPreferencesUiState {
        return NotificationPreferencesUiState(
            membershipEnabled = prefs.membershipEnabled,
            expensesEnabled = prefs.expensesEnabled,
            financialEnabled = prefs.financialEnabled,
            timezone = user?.timezone ?: ZoneId.systemDefault().id,
            preferredReminderTime = user?.preferredReminderTime,
            isLoading = false
        )
    }

    override fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute)
    }
}
