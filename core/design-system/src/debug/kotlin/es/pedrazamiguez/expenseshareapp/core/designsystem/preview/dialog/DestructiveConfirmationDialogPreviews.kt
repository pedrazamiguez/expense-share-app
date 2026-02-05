package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.dialog

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun DestructiveConfirmationDialogPreview() {
    PreviewThemeWrapper {
        DestructiveConfirmationDialog(
            title = "Delete Group?",
            text = "Are you sure you want to delete \"Summer Trip 2026\"? This will remove all expenses and cannot be undone.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@PreviewComplete
@Composable
private fun DestructiveConfirmationDialogShortTextPreview() {
    PreviewThemeWrapper {
        DestructiveConfirmationDialog(
            title = "Delete Item?",
            text = "This action cannot be undone.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
