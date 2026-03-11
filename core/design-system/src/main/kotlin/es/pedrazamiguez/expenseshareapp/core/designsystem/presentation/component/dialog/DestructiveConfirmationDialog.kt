package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.R

/**
 * A Material 3 destructive confirmation dialog.
 * Used as a safety check before irreversible actions like deletion or sign-out.
 *
 * @param title The dialog title (e.g., "Delete Group?").
 * @param text The dialog body text explaining the consequence.
 * @param onConfirm Called when the user confirms the destructive action.
 * @param onDismiss Called when the user cancels or dismisses the dialog.
 * @param confirmLabel The label for the confirm button. Defaults to "Delete".
 */
@Composable
fun DestructiveConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(id = R.string.action_delete),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}
