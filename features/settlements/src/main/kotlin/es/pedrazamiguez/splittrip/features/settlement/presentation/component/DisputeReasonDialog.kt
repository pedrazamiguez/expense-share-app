package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.features.settlement.R

@Composable
internal fun DisputeReasonDialog(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.your_position_dispute_dialog_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                StyledOutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    placeholder = stringResource(R.string.your_position_dispute_dialog_hint),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = reason.isNotBlank()
            ) {
                Text(text = stringResource(R.string.your_position_dispute_dialog_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.your_position_dispute_dialog_cancel))
            }
        },
        modifier = modifier
    )
}
