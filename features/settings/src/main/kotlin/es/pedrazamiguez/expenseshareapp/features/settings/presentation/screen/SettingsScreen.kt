package es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.theme.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.LogoutButton
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.SettingsTopAppBar
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.settingsSections
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.data.buildSettingsSections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    hasNotificationPermission: Boolean = false,
    currentCurrency: Currency? = null,
    onDefaultCurrencyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SettingsTopAppBar(
                    scrollBehavior = scrollBehavior,
                    onBack = onBack
                )
            }
        ) { innerPadding ->
            val sections = buildSettingsSections(
                onNotificationsClick = onNotificationsClick,
                hasNotificationPermission = hasNotificationPermission,
                currentCurrency = currentCurrency,
                onDefaultCurrencyClick = onDefaultCurrencyClick
            )

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                settingsSections(sections)

                item(key = "logout_button") {
                    LogoutButton { onLogoutClick() }
                }
            }
        }
    }
}

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
