package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    title: String? = null
) {
    val todayUtcMillis = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    val minMillis = minDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    val maxMillis = maxDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: todayUtcMillis
    val minYear = minDate?.year
    val maxYear = maxDate?.year ?: LocalDate.now().year

    val initialSelected = resolveInitialSelected(initialDateMillis, minMillis, maxMillis, todayUtcMillis)
    val selectableDates = createSelectableDates(minMillis, maxMillis, minYear, maxYear)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelected,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = title?.let {
                {
                    Text(
                        text = it,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

private fun resolveInitialSelected(
    initialDateMillis: Long?,
    minMillis: Long?,
    maxMillis: Long,
    todayUtcMillis: Long
): Long = initialDateMillis?.let {
    Instant.ofEpochMilli(it)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
} ?: if (todayUtcMillis in (minMillis ?: Long.MIN_VALUE)..maxMillis) {
    todayUtcMillis
} else {
    minMillis ?: maxMillis
}

@OptIn(ExperimentalMaterial3Api::class)
private fun createSelectableDates(
    minMillis: Long?,
    maxMillis: Long,
    minYear: Int?,
    maxYear: Int
): SelectableDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val afterMin = minMillis?.let { utcTimeMillis >= it } ?: true
        val beforeMax = utcTimeMillis <= maxMillis
        return afterMin && beforeMax
    }

    override fun isSelectableYear(year: Int): Boolean {
        val afterMinYear = minYear?.let { year >= it } ?: true
        val beforeMaxYear = year <= maxYear
        return afterMinYear && beforeMaxYear
    }
}
