package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingBank
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Receipt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.component.NotificationCategoryItem
import es.pedrazamiguez.splittrip.features.settings.presentation.component.ReminderTimePickerDialog
import es.pedrazamiguez.splittrip.features.settings.presentation.component.TimezoneSelectionBottomSheet
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("LongMethod") // Compose UI builder DSL
@Composable
fun NotificationPreferencesScreen(
    uiState: NotificationPreferencesUiState = NotificationPreferencesUiState(),
    onEvent: (NotificationPreferencesUiEvent) -> Unit = {}
) {
    var showTimezoneSheet by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var timezones by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        timezones = withContext(Dispatchers.Default) {
            ZoneId.getAvailableZoneIds().toList().sorted()
        }
    }

    if (showTimezoneSheet) {
        TimezoneSelectionBottomSheet(
            timezones = timezones,
            onTimezoneSelected = { zone ->
                onEvent(NotificationPreferencesUiEvent.UpdateTimezone(zone))
            },
            onDismiss = { showTimezoneSheet = false }
        )
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            preferredReminderTime = uiState.preferredReminderTime,
            onTimeConfirm = { hour, minute ->
                onEvent(NotificationPreferencesUiEvent.UpdateReminderTime(hour, minute))
            },
            onDismiss = { showTimePicker = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.Small),
        contentPadding = PaddingValues(vertical = MaterialTheme.spacing.Small)
    ) {
        item(key = "header") {
            BodyText(
                text = stringResource(R.string.notification_prefs_header),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.Default,
                    vertical = MaterialTheme.spacing.Medium
                )
            )
        }

        item(key = "membership") {
            NotificationCategoryItem(
                icon = { Icon(TablerIcons.Outline.UsersGroup, contentDescription = null) },
                title = stringResource(R.string.notification_prefs_membership_title),
                description = stringResource(R.string.notification_prefs_membership_description),
                checked = uiState.membershipEnabled,
                onCheckedChange = { enabled ->
                    onEvent(
                        NotificationPreferencesUiEvent.ToggleCategory(
                            NotificationCategory.MEMBERSHIP,
                            enabled
                        )
                    )
                }
            )
        }

        item(key = "expenses") {
            NotificationCategoryItem(
                icon = { Icon(TablerIcons.Outline.Receipt, contentDescription = null) },
                title = stringResource(R.string.notification_prefs_expenses_title),
                description = stringResource(R.string.notification_prefs_expenses_description),
                checked = uiState.expensesEnabled,
                onCheckedChange = { enabled ->
                    onEvent(
                        NotificationPreferencesUiEvent.ToggleCategory(
                            NotificationCategory.EXPENSES,
                            enabled
                        )
                    )
                }
            )
        }

        item(key = "financial") {
            NotificationCategoryItem(
                icon = { Icon(TablerIcons.Outline.BuildingBank, contentDescription = null) },
                title = stringResource(R.string.notification_prefs_financial_title),
                description = stringResource(R.string.notification_prefs_financial_description),
                checked = uiState.financialEnabled,
                onCheckedChange = { enabled ->
                    onEvent(
                        NotificationPreferencesUiEvent.ToggleCategory(
                            NotificationCategory.FINANCIAL,
                            enabled
                        )
                    )
                }
            )
        }

        item(key = "reminder_prefs_header") {
            BodyText(
                text = stringResource(R.string.notification_prefs_reminder_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.Default,
                    vertical = MaterialTheme.spacing.Medium
                )
            )
        }

        item(key = "timezone") {
            Box(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.Default)
            ) {
                StyledOutlinedTextField(
                    value = uiState.timezone ?: stringResource(R.string.notification_prefs_select_timezone),
                    onValueChange = {},
                    readOnly = true,
                    focusable = false,
                    label = stringResource(R.string.notification_prefs_timezone),
                    onClick = { showTimezoneSheet = true }
                )
            }
        }

        item(key = "reminder_time") {
            Box(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.Default)
            ) {
                StyledOutlinedTextField(
                    value = uiState.preferredReminderTime ?: "",
                    onValueChange = { },
                    readOnly = true,
                    focusable = false,
                    label = stringResource(R.string.notification_prefs_reminder_time),
                    onClick = { showTimePicker = true }
                )
            }
        }
    }
}
