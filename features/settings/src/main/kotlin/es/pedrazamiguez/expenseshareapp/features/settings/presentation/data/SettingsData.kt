package es.pedrazamiguez.expenseshareapp.features.settings.presentation.data

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.getNameRes
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import es.pedrazamiguez.expenseshareapp.features.settings.R
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature.AppVersionFeature
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature.InstallationIdFeature
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.SettingsItemModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.SettingsSectionModel

/**
 * Builds the settings sections with all configuration.
 * This function centralizes the settings data, making it easy to:
 * - Add, remove, or reorder settings
 * - Modify settings behavior
 * - Test settings configuration
 */
@Composable
fun buildSettingsSections(
    onNotificationsClick: () -> Unit,
    hasNotificationPermission: Boolean,
    currentCurrency: Currency?,
    onDefaultCurrencyClick: () -> Unit
): List<SettingsSectionModel> = listOf(
    accountSection(),
    preferencesSection(
        onNotificationsClick = onNotificationsClick,
        hasNotificationPermission = hasNotificationPermission,
        currentCurrency = currentCurrency,
        onDefaultCurrencyClick = onDefaultCurrencyClick
    ),
    developerSection(),
    supportSection(),
    aboutSection()
)

private fun accountSection() = SettingsSectionModel(
    titleRes = R.string.settings_section_account,
    items = listOf(
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Info,
            titleRes = R.string.settings_account_status_title,
            descriptionRes = R.string.settings_account_status_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.CreditCard,
            titleRes = R.string.settings_account_subscriptions_title,
            descriptionRes = R.string.settings_account_subscriptions_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Shield,
            titleRes = R.string.settings_account_security_title,
            descriptionRes = R.string.settings_account_security_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Email,
            titleRes = R.string.settings_account_email_title,
            descriptionRes = R.string.settings_account_email_description
        )
    )
)

private fun preferencesSection(
    onNotificationsClick: () -> Unit,
    hasNotificationPermission: Boolean,
    currentCurrency: Currency?,
    onDefaultCurrencyClick: () -> Unit
) = SettingsSectionModel(
    titleRes = R.string.settings_section_preferences,
    items = listOf(
        SettingsItemModel.Standard(
            icon = Icons.Outlined.DarkMode,
            titleRes = R.string.settings_preferences_theme_title,
            descriptionRes = R.string.settings_preferences_theme_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Translate,
            titleRes = R.string.settings_preferences_language_title,
            descriptionRes = R.string.settings_preferences_language_description
        ),
        SettingsItemModel.WithTrailing(
            icon = Icons.Outlined.Notifications,
            titleRes = R.string.settings_preferences_notifications_title,
            descriptionRes = R.string.settings_preferences_notifications_description,
            onClick = onNotificationsClick,
            trailingContent = {
                Switch(
                    checked = hasNotificationPermission,
                    onCheckedChange = { onNotificationsClick() }
                )
            }
        ),
        SettingsItemModel.WithCustomDescription(
            icon = Icons.Outlined.EuroSymbol,
            titleRes = R.string.settings_preferences_currency_title,
            onClick = onDefaultCurrencyClick,
            descriptionContent = {
                CurrencyDescription(currentCurrency)
            }
        )
    )
)

@Composable
private fun CurrencyDescription(currentCurrency: Currency?) {
    Crossfade(
        targetState = currentCurrency,
        label = "CurrencyFade"
    ) { currency ->
        if (currency == null) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
            )
        } else {
            val currencyName = stringResource(id = currency.getNameRes())
            Text(text = "$currencyName (${currency.symbol})")
        }
    }
}

private fun developerSection() = SettingsSectionModel(
    titleRes = R.string.settings_section_developer,
    items = listOf(
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Layers,
            titleRes = R.string.settings_developer_layout_title,
            descriptionRes = R.string.settings_developer_layout_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Widgets,
            titleRes = R.string.settings_developer_widgets_title,
            descriptionRes = R.string.settings_developer_widgets_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Image,
            titleRes = R.string.settings_developer_assets_title,
            descriptionRes = R.string.settings_developer_assets_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Build,
            titleRes = R.string.settings_developer_services_title,
            descriptionRes = R.string.settings_developer_services_description
        )
    )
)

private fun supportSection() = SettingsSectionModel(
    titleRes = R.string.settings_section_support,
    items = listOf(
        SettingsItemModel.Standard(
            icon = Icons.Outlined.BugReport,
            titleRes = R.string.settings_support_bug_title,
            descriptionRes = R.string.settings_support_bug_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.Lightbulb,
            titleRes = R.string.settings_support_feature_title,
            descriptionRes = R.string.settings_support_feature_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            titleRes = R.string.settings_support_faq_title,
            descriptionRes = R.string.settings_support_faq_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.SupportAgent,
            titleRes = R.string.settings_support_support_title,
            descriptionRes = R.string.settings_support_support_description
        )
    )
)

private fun aboutSection() = SettingsSectionModel(
    titleRes = R.string.settings_section_about,
    items = listOf(
        SettingsItemModel.Custom { AppVersionFeature() },
        SettingsItemModel.Custom { InstallationIdFeature() },
        SettingsItemModel.Standard(
            icon = Icons.Outlined.PrivacyTip,
            titleRes = R.string.settings_about_privacy_title,
            descriptionRes = R.string.settings_about_privacy_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            titleRes = R.string.settings_about_libraries_title,
            descriptionRes = R.string.settings_about_libraries_description
        ),
        SettingsItemModel.Standard(
            icon = Icons.Outlined.PersonPin,
            titleRes = R.string.settings_about_developer_title,
            descriptionRes = R.string.settings_about_developer_description
        )
    )
)

