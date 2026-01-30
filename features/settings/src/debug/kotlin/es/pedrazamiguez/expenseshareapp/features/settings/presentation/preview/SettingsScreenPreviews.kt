package es.pedrazamiguez.expenseshareapp.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.SettingsScreen

@PreviewComplete
@Composable
private fun SettingsScreenPreview() {
    PreviewThemeWrapper {
        SettingsScreen(
            hasNotificationPermission = true, currentCurrency = Currency.JPY
        )
    }
}
