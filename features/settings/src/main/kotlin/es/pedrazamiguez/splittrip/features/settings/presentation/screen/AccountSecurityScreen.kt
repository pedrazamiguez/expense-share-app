package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.features.settings.presentation.component.AccountSecurityContent
import es.pedrazamiguez.splittrip.features.settings.presentation.component.PasswordResetConfirmDialog
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.AccountSecurityUiMapper
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event.AccountSecurityUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.AccountSecurityUiState

@Composable
fun AccountSecurityScreen(
    uiState: AccountSecurityUiState,
    uiMapper: AccountSecurityUiMapper,
    onEvent: (AccountSecurityUiEvent) -> Unit
) {
    DeferredLoadingContainer(
        isLoading = uiState.isLoading,
        loadingContent = { ShimmerLoadingList() }
    ) {
        AccountSecurityContent(
            uiState = uiState,
            uiMapper = uiMapper,
            onEvent = onEvent
        )
    }

    if (uiState.showPasswordResetConfirmDialog) {
        PasswordResetConfirmDialog(
            email = uiState.email,
            onConfirm = { onEvent(AccountSecurityUiEvent.ConfirmSendPasswordReset) },
            onDismiss = { onEvent(AccountSecurityUiEvent.DismissPasswordResetConfirmation) }
        )
    }
}
