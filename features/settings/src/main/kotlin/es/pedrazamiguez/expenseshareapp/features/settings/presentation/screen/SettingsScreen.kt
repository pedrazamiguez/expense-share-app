package es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.theme.ExpenseShareAppTheme
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

    // Use the derived state directly - no animation delay needed
    // The scroll itself provides smooth interpolation
    val collapseFraction by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction }
    }

    // Dynamic title color that transitions from primary to onSurface as it collapses
    val expandedTitleColor = MaterialTheme.colorScheme.primary
    val collapsedTitleColor = MaterialTheme.colorScheme.onSurface
    val titleColor = lerp(expandedTitleColor, collapsedTitleColor, collapseFraction)

    // Subtitle alpha - fades out as the app bar collapses
    val subtitleAlpha = 1f - collapseFraction

    // Subtitle height - shrinks as the app bar collapses
    val subtitleMaxHeight = 20.dp // Approximate height of bodyMedium text
    val subtitleHeight = subtitleMaxHeight * subtitleAlpha

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.settings_title),
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            // Animate both alpha and height for smooth collapse
                            // The height animation ensures the layout actually shrinks
                            Text(
                                text = stringResource(R.string.settings_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .height(subtitleHeight)
                                    .graphicsLayer {
                                        alpha = subtitleAlpha
                                    }
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.settings_back)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
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
