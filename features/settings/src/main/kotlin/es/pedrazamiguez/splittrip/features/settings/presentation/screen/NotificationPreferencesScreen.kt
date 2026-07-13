package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingBank
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Receipt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.component.NotificationCategoryItem
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.model.NotificationPreferencesUiState

@Suppress("LongMethod") // Compose UI builder DSL
@Composable
fun NotificationPreferencesScreen(
    uiState: NotificationPreferencesUiState = NotificationPreferencesUiState(),
    onEvent: (NotificationPreferencesUiEvent) -> Unit = {}
) {
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
                text = "Reminder Preferences",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.Default,
                    vertical = MaterialTheme.spacing.Medium
                )
            )
        }

        item(key = "timezone") {
            val zones = androidx.compose.runtime.remember { java.time.ZoneId.getAvailableZoneIds().toList().sorted() }
            var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.Default)
            ) {
                es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField(
                    value = uiState.timezone ?: "Select Timezone",
                    onValueChange = {},
                    readOnly = true,
                    focusable = false,
                    label = "Timezone"
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    zones.forEach { zone ->
                        DropdownMenuItem(
                            text = { Text(zone) },
                            onClick = {
                                expanded = false
                                onEvent(
                                    NotificationPreferencesUiEvent.UpdateReminderPreferences(
                                        zone,
                                        uiState.preferredReminderTime
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        item(key = "reminder_time") {
            es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField(
                value = uiState.preferredReminderTime ?: "",
                onValueChange = { time ->
                    onEvent(NotificationPreferencesUiEvent.UpdateReminderPreferences(uiState.timezone, time))
                },
                label = "Reminder Time (HH:MM)",
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.Default)
            )
        }
    }
}
