package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.AccountSecurityUiMapper
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event.AccountSecurityUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.AccountSecurityUiState

@Composable
fun AccountSecurityContent(
    uiState: AccountSecurityUiState,
    uiMapper: AccountSecurityUiMapper,
    onEvent: (AccountSecurityUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.Medium,
            vertical = MaterialTheme.spacing.Default
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        item(key = "auth_status_card") {
            AuthStatusCard(
                uiState = uiState,
                uiMapper = uiMapper
            )
        }

        item(key = "password_reset_card") {
            PasswordResetCard(
                uiState = uiState,
                onResetClick = { onEvent(AccountSecurityUiEvent.RequestPasswordResetConfirmation) }
            )
        }

        item(key = "biometric_lock_card") {
            BiometricLockCard(
                biometricLockEnabled = uiState.biometricLockEnabled,
                onToggle = { onEvent(AccountSecurityUiEvent.ToggleBiometricLock(it)) }
            )
        }

        item(key = "linked_providers_card") {
            LinkedProvidersCard(
                onManageProvidersClick = { onEvent(AccountSecurityUiEvent.NavigateToAccountStatus) }
            )
        }
    }
}
