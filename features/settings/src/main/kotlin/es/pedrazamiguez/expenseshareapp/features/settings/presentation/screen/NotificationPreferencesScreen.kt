package es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationCategory
import es.pedrazamiguez.expenseshareapp.features.settings.R
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.NotificationPreferencesUiEvent
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.NotificationPreferencesUiState

@Composable
fun NotificationPreferencesScreen(
    uiState: NotificationPreferencesUiState = NotificationPreferencesUiState(),
    onEvent: (NotificationPreferencesUiEvent) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item(key = "header") {
            Text(
                text = stringResource(R.string.notification_prefs_header),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        item(key = "membership") {
            NotificationCategoryItem(
                icon = { Icon(Icons.Outlined.Groups, contentDescription = null) },
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
                icon = { Icon(Icons.Outlined.Receipt, contentDescription = null) },
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
                icon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null) },
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
    }
}

@Composable
private fun NotificationCategoryItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        leadingContent = icon,
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}
