package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Calendar
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.AppDatePickerDialog
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.AppTimePickerDialog
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CardSectionLabelText
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Reusable date and time picker section.
 * Shows a read-only text field that opens a Material 3 DatePickerDialog on tap,
 * followed by a TimePicker in an AlertDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateTimeSelectionSection(
    title: String,
    label: String,
    formattedDateTime: String,
    isDateTimeValid: Boolean,
    dateTimeMillis: Long?,
    onDateTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDateMillis by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        CardSectionLabelText(
            text = title
        )
        StyledOutlinedTextField(
            value = formattedDateTime,
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { Icon(TablerIcons.Outline.Calendar, null) },
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            isError = !isDateTimeValid
        )
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = dateTimeMillis,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                tempSelectedDateMillis = selectedDate
                showDatePicker = false
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialTimeMillis = dateTimeMillis,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                val localDate = if (tempSelectedDateMillis != null) {
                    // DatePicker returns UTC midnight, so ZoneOffset.UTC extracts the correct exact calendar day
                    Instant.ofEpochMilli(tempSelectedDateMillis!!)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                } else {
                    // Fallback uses the current device instant, so we use systemDefault()
                    Instant.ofEpochMilli(System.currentTimeMillis())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                val localDateTime = localDate.atTime(hour, minute)
                // Convert the user's intended local date/time into a correct absolute UTC timestamp
                val combinedMillis = localDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                onDateTimeSelected(combinedMillis)
                showTimePicker = false
            }
        )
    }
}
