package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
                            contentDescription = stringResource(R.string.settings_back)
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
                SettingsSection(title = stringResource(R.string.settings_section_account))
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.settings_account_status_title),
                        description = stringResource(R.string.settings_account_status_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.CreditCard,
                        title = stringResource(R.string.settings_account_subscriptions_title),
                        description = stringResource(R.string.settings_account_subscriptions_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Shield,
                        title = stringResource(R.string.settings_account_security_title),
                        description = stringResource(R.string.settings_account_security_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Email,
                        title = stringResource(R.string.settings_account_email_title),
                        description = stringResource(R.string.settings_account_email_description),
                        onClick = {})
                )
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_preferences))
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.settings_preferences_theme_title),
                        description = stringResource(R.string.settings_preferences_theme_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Translate,
                        title = stringResource(R.string.settings_preferences_language_title),
                        description = stringResource(R.string.settings_preferences_language_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.settings_preferences_notifications_title),
                        description = stringResource(R.string.settings_preferences_notifications_description),
                        onClick = onNotificationsClick
                    )
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.EuroSymbol,
                        title = stringResource(R.string.settings_preferences_currency_title),
                        description = stringResource(R.string.settings_preferences_currency_description),
                        onClick = {})
                )
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_developer))
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Layers,
                        title = stringResource(R.string.settings_developer_layout_title),
                        description = stringResource(R.string.settings_developer_layout_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Widgets,
                        title = stringResource(R.string.settings_developer_widgets_title),
                        description = stringResource(R.string.settings_developer_widgets_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Image,
                        title = stringResource(R.string.settings_developer_assets_title),
                        description = stringResource(R.string.settings_developer_assets_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Build,
                        title = stringResource(R.string.settings_developer_services_title),
                        description = stringResource(R.string.settings_developer_services_description),
                        onClick = {})
                )
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_support))
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.BugReport,
                        title = stringResource(R.string.settings_support_bug_title),
                        description = stringResource(R.string.settings_support_bug_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.Lightbulb,
                        title = stringResource(R.string.settings_support_feature_title),
                        description = stringResource(R.string.settings_support_feature_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = stringResource(R.string.settings_support_faq_title),
                        description = stringResource(R.string.settings_support_faq_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.SupportAgent,
                        title = stringResource(R.string.settings_support_support_title),
                        description = stringResource(R.string.settings_support_support_description),
                        onClick = {})
                )
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_about))
            }
            item {
                AppVersionFeature()
            }
            item {
                InstallationIdFeature()
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.PrivacyTip,
                        title = stringResource(R.string.settings_about_privacy_title),
                        description = stringResource(R.string.settings_about_privacy_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = stringResource(R.string.settings_about_libraries_title),
                        description = stringResource(R.string.settings_about_libraries_description),
                        onClick = {})
                )
            }
            item {
                SettingsRow(
                    SettingItemView(
                        icon = Icons.Outlined.PersonPin,
                        title = stringResource(R.string.settings_about_developer_title),
                        description = stringResource(R.string.settings_about_developer_description),
                        onClick = {})
                )
            }

            item {
                LogoutButton { onLogoutClick() }
            }
        }

    }
}
