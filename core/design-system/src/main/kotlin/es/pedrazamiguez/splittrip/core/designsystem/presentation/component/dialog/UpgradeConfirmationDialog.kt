package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.R

/**
 * A Material 3 confirmation dialog prompting the user to upgrade to SplitTrip Pro.
 * Displayed when an action is blocked by a subscription feature gate or limit.
 *
 * @param title The dialog title (e.g. "SplitTrip Pro").
 * @param text The explanatory message detailing the limit or restricted feature.
 * @param onUpgrade Callback invoked when the user confirms upgrade.
 * @param onDismiss Callback invoked when the user dismisses the dialog.
 * @param upgradeLabel The label for the primary upgrade button.
 * @param dismissLabel The label for the secondary dismiss button.
 */
@Composable
fun UpgradeConfirmationDialog(
    title: String,
    text: String,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit,
    upgradeLabel: String = stringResource(id = R.string.upgrade_dialog_confirm),
    dismissLabel: String = stringResource(id = R.string.upgrade_dialog_dismiss)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(upgradeLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        }
    )
}
