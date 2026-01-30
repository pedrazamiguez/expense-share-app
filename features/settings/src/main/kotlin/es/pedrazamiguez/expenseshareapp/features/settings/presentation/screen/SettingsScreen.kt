package es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import es.pedrazamiguez.expenseshareapp.features.settings.R
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.LogoutButton
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
                DynamicTopAppBar(
                    title = stringResource(R.string.settings_title),
                    subtitle = stringResource(R.string.settings_subtitle),
                    onBack = onBack,
                    scrollBehavior = scrollBehavior
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

