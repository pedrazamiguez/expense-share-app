package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
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
        ModalBottomSheet(
            onDismissRequest = { showTimezoneSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            var searchQuery by remember { mutableStateOf("") }
            val filteredZones = remember(searchQuery, timezones) {
                if (searchQuery.isBlank()) {
                    timezones
                } else {
                    timezones.filter {
                        it.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                StyledOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = stringResource(R.string.notification_prefs_select_timezone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.Default, vertical = MaterialTheme.spacing.Small)
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredZones, key = { it }) { zone ->
                        ListItem(
                            headlineContent = { Text(zone) },
                            modifier = Modifier.clickable {
                                onEvent(
                                    NotificationPreferencesUiEvent.UpdateReminderPreferences(
                                        zone,
                                        uiState.preferredReminderTime
                                    )
                                )
                                showTimezoneSheet = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val initialTime = remember(uiState.preferredReminderTime) {
            try {
                if (!uiState.preferredReminderTime.isNullOrBlank()) {
                    LocalTime.parse(uiState.preferredReminderTime)
                } else {
                    LocalTime.now()
                }
            } catch (e: Exception) {
                LocalTime.now()
            }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val timeString = String.format(
                        Locale.ROOT,
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onEvent(NotificationPreferencesUiEvent.UpdateReminderPreferences(uiState.timezone, timeString))
                    showTimePicker = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
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
