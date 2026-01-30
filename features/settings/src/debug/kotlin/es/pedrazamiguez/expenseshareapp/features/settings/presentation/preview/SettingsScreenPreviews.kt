package es.pedrazamiguez.expenseshareapp.features.settings.presentation.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.SettingsScreen

@Preview(
    name = "English - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "en"
)
@Preview(
    name = "English - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
@Preview(
    name = "Español - Claro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "es"
)
@Preview(
    name = "Español - Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "es"
)
@Composable
private fun SettingsScreenPreview() {
    ExpenseShareAppTheme {
        SettingsScreen(
            hasNotificationPermission = true, currentCurrency = Currency.JPY
        )
    }
}
