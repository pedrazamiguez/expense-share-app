package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.common.presentation.asString
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.UpgradeConfirmationDialog
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.CreateEditGroupUiState

@Suppress("LongParameterList")
@Composable
fun CreateEditGroupOverlays(
    state: CreateEditGroupUiState,
    showScanner: Boolean,
    showExitConfirmation: Boolean,
    onDismissScanner: () -> Unit,
    onMemberScanned: (userId: String, email: String) -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExit: () -> Unit,
    onUpgrade: () -> Unit,
    onDismissUpgrade: () -> Unit
) {
    val context = LocalContext.current

    if (showScanner) {
        QrScannerDialog(
            onDismissRequest = onDismissScanner,
            onScanned = { payload -> onMemberScanned(payload.userId, payload.email) }
        )
    }

    if (showExitConfirmation) {
        DestructiveConfirmationDialog(
            title = stringResource(DesignSystemR.string.wizard_exit_dialog_title),
            text = stringResource(DesignSystemR.string.wizard_exit_dialog_message),
            confirmLabel = stringResource(DesignSystemR.string.wizard_exit_dialog_confirm),
            onConfirm = onConfirmExit,
            onDismiss = onDismissExit
        )
    }

    if (state.showUpgradeDialog) {
        UpgradeConfirmationDialog(
            title = state.upgradeDialogTitle?.asString(context)
                ?: stringResource(DesignSystemR.string.upgrade_dialog_title),
            text = state.upgradeDialogMessage?.asString(context) ?: "",
            onUpgrade = onUpgrade,
            onDismiss = onDismissUpgrade
        )
    }
}
