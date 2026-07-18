package es.pedrazamiguez.splittrip.features.settings.presentation.model

import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory

data class NotificationPreferencesUiState(
    val membershipEnabled: Boolean = true,
    val expensesEnabled: Boolean = true,
    val financialEnabled: Boolean = true,
    val timezone: String? = null,
    val preferredReminderTime: String? = null,
    val isLoading: Boolean = true
)

sealed interface NotificationPreferencesUiEvent {
    data class ToggleCategory(val category: NotificationCategory, val enabled: Boolean) : NotificationPreferencesUiEvent
    data class UpdateTimezone(val timezone: String?) : NotificationPreferencesUiEvent
    data class UpdateReminderTime(val hour: Int, val minute: Int) : NotificationPreferencesUiEvent
}
