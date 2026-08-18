package es.pedrazamiguez.splittrip.features.expense.presentation.component.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.extension.debouncedClickable
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Calendar
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.X
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.AppDatePickerDialog
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.features.expense.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Suppress("LongMethod", "CognitiveComplexMethod")
@Composable
fun DateRangeFilterSection(
    criteria: ExpenseFilterCriteria,
    onCriteriaChange: (ExpenseFilterCriteria) -> Unit,
    oldestExpenseDate: LocalDate?,
    newestExpenseDate: LocalDate?,
    formattedStartDate: String,
    formattedEndDate: String,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    SectionCard(
        title = stringResource(R.string.expenses_filter_section_date_range),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .debouncedClickable { showStartDatePicker = true },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.Medium)
                ) {
                    Text(
                        text = stringResource(R.string.expenses_filter_date_from),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
                        ) {
                            Icon(
                                imageVector = TablerIcons.Outline.Calendar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (formattedStartDate.isNotBlank()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = formattedStartDate.ifBlank {
                                    stringResource(R.string.expenses_filter_date_select)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (formattedStartDate.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (formattedStartDate.isNotBlank()) {
                            IconButton(
                                onClick = { onCriteriaChange(criteria.copy(startDate = null)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Outline.X,
                                    contentDescription = stringResource(
                                        R.string.expenses_filter_date_clear_start_cd
                                    ),
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .debouncedClickable { showEndDatePicker = true },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.Medium)
                ) {
                    Text(
                        text = stringResource(R.string.expenses_filter_date_to),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
                        ) {
                            Icon(
                                imageVector = TablerIcons.Outline.Calendar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (formattedEndDate.isNotBlank()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = formattedEndDate.ifBlank {
                                    stringResource(R.string.expenses_filter_date_select)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (formattedEndDate.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (formattedEndDate.isNotBlank()) {
                            IconButton(
                                onClick = { onCriteriaChange(criteria.copy(endDate = null)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = TablerIcons.Outline.X,
                                    contentDescription = stringResource(
                                        R.string.expenses_filter_date_clear_end_cd
                                    ),
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        val initialStartMillis = criteria.startDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
        AppDatePickerDialog(
            initialDateMillis = initialStartMillis,
            minDate = oldestExpenseDate,
            maxDate = criteria.endDate ?: newestExpenseDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { selectedMillis ->
                val selectedDate = Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate()
                onCriteriaChange(criteria.copy(startDate = selectedDate))
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        val initialEndMillis = criteria.endDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
        AppDatePickerDialog(
            initialDateMillis = initialEndMillis,
            minDate = criteria.startDate ?: oldestExpenseDate,
            maxDate = newestExpenseDate,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { selectedMillis ->
                val selectedDate = Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate()
                onCriteriaChange(criteria.copy(endDate = selectedDate))
                showEndDatePicker = false
            }
        )
    }
}
