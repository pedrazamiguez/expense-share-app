package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EuroSymbol
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.ui.settings.R
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.LogoutButton
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.SettingsRow
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.SettingsSection
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature.AppVersionFeature
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature.InstallationIdFeature
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.view.SettingItemView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back".hardcoded
                        )
                    }
                })
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SettingsSection(title = "Account".hardcoded)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.Info,
                        title = "Status".hardcoded,
                        description = "Account info and verification".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.CreditCard,
                        title = "Subscriptions".hardcoded,
                        description = "Manage your plan".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Shield,
                        title = "Security & Privacy".hardcoded,
                        description = "Password, 2FA, privacy".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Email,
                        title = "Email & Communications".hardcoded,
                        description = "Email settings".hardcoded,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "Preferences".hardcoded)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.DarkMode,
                        title = "Theme".hardcoded,
                        description = "Light, dark, or system".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Translate,
                        title = "Language".hardcoded,
                        description = "App language".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications".hardcoded,
                        description = "Reminders & alerts".hardcoded,
                        onClick = onNotificationsClick
                    ),
                    SettingItemView(
                        icon = Icons.Outlined.EuroSymbol,
                        title = "Default Currency".hardcoded,
                        description = "Preferred currency".hardcoded,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "Developer Options".hardcoded)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.Layers,
                        title = "Layout".hardcoded,
                        description = "Test UI layouts".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Widgets,
                        title = "Widgets".hardcoded,
                        description = "Preview UI components".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Image,
                        title = "Assets".hardcoded,
                        description = "Manage images & animations".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Build,
                        title = "Services Playground".hardcoded,
                        description = "Test backend services".hardcoded,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "Support & Feedback".hardcoded)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.BugReport,
                        title = "Report a Bug".hardcoded,
                        description = "Fix issues".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Lightbulb,
                        title = "Feature request".hardcoded,
                        description = "Suggest ideas".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "FAQs".hardcoded,
                        description = "Common questions".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.SupportAgent,
                        title = "Support".hardcoded,
                        description = "Get help".hardcoded,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "About".hardcoded)
            }
            item {
                AppVersionFeature()
            }
            item {
                InstallationIdFeature()
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy Policy".hardcoded,
                        description = "Data usage".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = "Open Source Libraries".hardcoded,
                        description = "Third-party licenses".hardcoded,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.PersonPin,
                        title = "Developer".hardcoded,
                        description = "About the developer".hardcoded,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                LogoutButton { onLogoutClick() }
            }
        }

    }
}
