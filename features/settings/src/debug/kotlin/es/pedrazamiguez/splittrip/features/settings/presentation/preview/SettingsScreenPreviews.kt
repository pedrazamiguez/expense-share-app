package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.Currency
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.SettingsScreen

@PreviewComplete
@Composable
private fun SettingsScreenPreview() {
    PreviewThemeWrapper {
        SettingsScreen(
            hasNotificationPermission = true,
            currentCurrency = Currency.JPY
        )
    }
}
