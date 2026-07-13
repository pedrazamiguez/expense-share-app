package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePickerDialog(
    preferredReminderTime: String?,
    onTimeConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTime = remember(preferredReminderTime) {
        try {
            if (!preferredReminderTime.isNullOrBlank()) {
                LocalTime.parse(preferredReminderTime)
            } else {
                LocalTime.now()
            }
        } catch (e: Exception) {
            LocalTime.now()
        }
    }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
