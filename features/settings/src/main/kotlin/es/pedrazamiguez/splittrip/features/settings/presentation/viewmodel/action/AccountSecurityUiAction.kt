package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface AccountSecurityUiAction {
    data class ShowTopPill(val message: UiText) : AccountSecurityUiAction
    data class NavigateToRoute(val route: String) : AccountSecurityUiAction
    data object NavigateBack : AccountSecurityUiAction
    data object RequestBiometricConfirmation : AccountSecurityUiAction
}
