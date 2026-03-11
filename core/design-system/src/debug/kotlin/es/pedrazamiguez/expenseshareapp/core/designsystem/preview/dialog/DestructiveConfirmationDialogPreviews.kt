package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.R
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun DestructiveConfirmationDialogPreview() {
    PreviewThemeWrapper {
        DestructiveConfirmationDialog(
            title = stringResource(R.string.preview_dialog_delete_title),
            text = stringResource(R.string.preview_dialog_delete_text),
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
            title = stringResource(R.string.preview_dialog_delete_short_title),
            text = stringResource(R.string.preview_dialog_delete_short_text),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@PreviewComplete
@Composable
private fun DestructiveConfirmationDialogCustomLabelPreview() {
    PreviewThemeWrapper {
        DestructiveConfirmationDialog(
            title = stringResource(R.string.preview_dialog_logout_title),
            text = stringResource(R.string.preview_dialog_logout_text),
            onConfirm = {},
            onDismiss = {},
            confirmLabel = stringResource(R.string.preview_dialog_logout_confirm)
        )
    }
}

