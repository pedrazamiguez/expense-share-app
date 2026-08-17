package es.pedrazamiguez.splittrip.features.expense.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.splittrip.features.expense.R

@Composable
fun ResetFiltersConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DestructiveConfirmationDialog(
        title = stringResource(R.string.expenses_filter_reset_dialog_title),
        text = stringResource(R.string.expenses_filter_reset_dialog_message),
        confirmLabel = stringResource(R.string.expenses_filter_reset_dialog_confirm),
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
