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
import androidx.compose.material.icons.outlined.Commit
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
import androidx.compose.material.icons.outlined.QrCode2
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
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.LogoutButton
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.SettingsRow
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.SettingsSection
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.view.SettingItemView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                SettingsSection(title = "Account".placeholder)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.Info,
                        title = "Status".placeholder,
                        description = "Account info and verification".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.CreditCard,
                        title = "Subscriptions".placeholder,
                        description = "Manage your plan".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Shield,
                        title = "Security & Privacy".placeholder,
                        description = "Password, 2FA, privacy".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Email,
                        title = "Email & Communications".placeholder,
                        description = "Email settings".placeholder,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "Preferences".placeholder)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.DarkMode,
                        title = "Theme".placeholder,
                        description = "Light, dark, or system".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Translate,
                        title = "Language".placeholder,
                        description = "App language".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications".placeholder,
                        description = "Reminders & alerts".placeholder,
                        onClick = onNotificationsClick
                    ),
                    SettingItemView(
                        icon = Icons.Outlined.EuroSymbol,
                        title = "Default Currency".placeholder,
                        description = "Preferred currency".placeholder,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "Developer Options".placeholder)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.Layers,
                        title = "Layout".placeholder,
                        description = "Test UI layouts".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Widgets,
                        title = "Widgets".placeholder,
                        description = "Preview UI components".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Image,
                        title = "Assets".placeholder,
                        description = "Manage images & animations".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Build,
                        title = "Services Playground".placeholder,
                        description = "Test backend services".placeholder,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "Support & Feedback".placeholder)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.BugReport,
                        title = "Report a Bug".placeholder,
                        description = "Fix issues".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.Lightbulb,
                        title = "Feature request".placeholder,
                        description = "Suggest ideas".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "FAQs".placeholder,
                        description = "Common questions".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.SupportAgent,
                        title = "Support".placeholder,
                        description = "Get help".placeholder,
                        onClick = {}),
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "About".placeholder)
            }
            items(
                listOf(
                    SettingItemView(
                        icon = Icons.Outlined.Commit,
                        title = "App Version".placeholder,
                        description = "Current version".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.QrCode2,
                        title = "Installation ID".placeholder,
                        description = "Unique identifier".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy Policy".placeholder,
                        description = "Data usage".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = "Open Source Libraries".placeholder,
                        description = "Third-party licenses".placeholder,
                        onClick = {}),
                    SettingItemView(
                        icon = Icons.Outlined.PersonPin,
                        title = "Developer".placeholder,
                        description = "About the developer".placeholder,
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
