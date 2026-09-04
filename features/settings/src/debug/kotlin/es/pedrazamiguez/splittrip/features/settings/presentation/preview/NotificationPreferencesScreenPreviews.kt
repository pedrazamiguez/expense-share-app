package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.NotificationPreferencesScreen

@PreviewComplete
@Composable
private fun NotificationPreferencesScreenPreview() {
    PreviewThemeWrapper {
        NotificationPreferencesScreen(
            uiState = NotificationPreferencesUiState(
                membershipEnabled = true,
                expensesEnabled = true,
                financialEnabled = false,
                timezone = "Europe/Madrid",
                preferredReminderTime = "09:00"
            ),
            onEvent = {}
        )
    }
}
