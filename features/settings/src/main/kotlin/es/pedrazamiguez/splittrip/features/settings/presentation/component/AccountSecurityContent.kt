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
            horizontal = MaterialTheme.spacing.ExtraLarge,
            vertical = MaterialTheme.spacing.ExtraLarge
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Section)
    ) {
        item(key = "identity_section") {
            AccountSecurityIdentitySection(
                uiState = uiState,
                uiMapper = uiMapper
            )
        }

        item(key = "security_preferences_card") {
            SecurityPreferencesCard(
                biometricLockEnabled = uiState.biometricLockEnabled,
                isBiometricToggleEnabled = uiState.isBiometricToggleEnabled,
                biometricSubtitle = uiMapper.formatBiometricSubtitle(uiState.biometricCapability),
                onBiometricLockToggle = { onEvent(AccountSecurityUiEvent.ToggleBiometricLock(it)) },
                onManageProvidersClick = { onEvent(AccountSecurityUiEvent.NavigateToAccountStatus) }
            )
        }

        item(key = "password_reset_section") {
            PasswordResetSection(
                uiState = uiState,
                onResetClick = { onEvent(AccountSecurityUiEvent.RequestPasswordResetConfirmation) }
            )
        }
    }
}
