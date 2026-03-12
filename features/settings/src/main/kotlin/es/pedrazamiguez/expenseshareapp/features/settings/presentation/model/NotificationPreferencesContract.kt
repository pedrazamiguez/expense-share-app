package es.pedrazamiguez.expenseshareapp.features.settings.presentation.model

import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationCategory

data class NotificationPreferencesUiState(
    val membershipEnabled: Boolean = true,
    val expensesEnabled: Boolean = true,
    val financialEnabled: Boolean = true,
    val isLoading: Boolean = true
)

sealed interface NotificationPreferencesUiEvent {
    data class ToggleCategory(
        val category: NotificationCategory,
        val enabled: Boolean
    ) : NotificationPreferencesUiEvent
}

