package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event

sealed interface AccountSecurityUiEvent {
    data object LoadAccountSecurity : AccountSecurityUiEvent
    data object RequestPasswordResetConfirmation : AccountSecurityUiEvent
    data object DismissPasswordResetConfirmation : AccountSecurityUiEvent
    data object ConfirmSendPasswordReset : AccountSecurityUiEvent
    data class ToggleBiometricLock(val enabled: Boolean) : AccountSecurityUiEvent
    data object BiometricConfirmationSuccess : AccountSecurityUiEvent
    data object NavigateToAccountStatus : AccountSecurityUiEvent
}
