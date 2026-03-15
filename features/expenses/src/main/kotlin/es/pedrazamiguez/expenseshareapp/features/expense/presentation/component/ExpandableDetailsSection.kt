package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Expandable details section of the Add Expense form.
 * Contains the progressive disclosure toggle and all detail fields:
 * Payment Method, Category, Split, Vendor, Notes, Payment Status, Due Date, and Receipt.
 *
 * The toggle state is managed internally with [rememberSaveable].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableDetailsSection(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Progressive Disclosure Toggle ─────────────────────────
        TextButton(
            onClick = { showDetails = !showDetails },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (showDetails) {
                    Icons.Default.ExpandLess
                } else {
                    Icons.Default.ExpandMore
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = if (showDetails) {
                    stringResource(R.string.add_expense_less_details)
                } else {
                    stringResource(R.string.add_expense_more_details)
                },
                style = MaterialTheme.typography.labelLarge
            )
        }

        // ── Expandable Detail Fields ──────────────────────────────
        AnimatedVisibility(
            visible = showDetails,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // ── Payment Method (Condensed) ────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.add_expense_payment_method_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CondensedChips(
                        items = uiState.paymentMethods,
                        selectedId = uiState.selectedPaymentMethod?.id,
                        onItemSelected = { methodId ->
                            onEvent(AddExpenseUiEvent.PaymentMethodSelected(methodId))
                            focusManager.clearFocus()
                        },
                        itemId = { it.id },
                        itemLabel = { it.displayText },
                        visibleCount = 3
                    )
                }

                // ── Category (Condensed) ──────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.add_expense_category_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CondensedChips(
                        items = uiState.availableCategories,
                        selectedId = uiState.selectedCategory?.id,
                        onItemSelected = { categoryId ->
                            onEvent(AddExpenseUiEvent.CategorySelected(categoryId))
                            focusManager.clearFocus()
                        },
                        itemId = { it.id },
                        itemLabel = { it.displayText },
                        visibleCount = 4
                    )
                }

                // ── Split Section ─────────────────────────────────
                if (uiState.availableSplitTypes.isNotEmpty() &&
                    uiState.memberIds.size > 1
                ) {
                    SplitSection(
                        uiState = uiState,
                        onEvent = onEvent
                    )
                }

                // ── Vendor / Company ──────────────────────────────
                StyledOutlinedTextField(
                    value = uiState.vendor,
                    onValueChange = { onEvent(AddExpenseUiEvent.VendorChanged(it)) },
                    label = stringResource(R.string.add_expense_vendor_label),
                    modifier = Modifier.fillMaxWidth(),
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )

                // ── Notes ─────────────────────────────────────────
                StyledOutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { onEvent(AddExpenseUiEvent.NotesChanged(it)) },
                    label = stringResource(R.string.add_expense_notes_label),
                    modifier = Modifier.fillMaxWidth(),
                    capitalization = KeyboardCapitalization.Sentences,
                    singleLine = false,
                    maxLines = 3,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                // ── Payment Status (Condensed) ────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.add_expense_payment_status_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CondensedChips(
                        items = uiState.availablePaymentStatuses,
                        selectedId = uiState.selectedPaymentStatus?.id,
                        onItemSelected = { statusId ->
                            onEvent(AddExpenseUiEvent.PaymentStatusSelected(statusId))
                            focusManager.clearFocus()
                        },
                        itemId = { it.id },
                        itemLabel = { it.displayText },
                        visibleCount = 3
                    )
                }

                // ── Due Date (Scheduled only) ─────────────────────
                AnimatedVisibility(visible = uiState.showDueDateSection) {
                    var showDatePicker by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                .copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                text = stringResource(R.string.add_expense_due_date_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            StyledOutlinedTextField(
                                value = uiState.formattedDueDate,
                                onValueChange = {},
                                readOnly = true,
                                label = stringResource(R.string.add_expense_due_date_label),
                                trailingIcon = {
                                    Icon(Icons.Default.CalendarToday, null)
                                },
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !uiState.isDueDateValid
                            )
                        }
                    }

                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = uiState.dueDateMillis
                        )
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            onEvent(
                                                AddExpenseUiEvent.DueDateSelected(millis)
                                            )
                                        }
                                        showDatePicker = false
                                    }
                                ) {
                                    Text(
                                        stringResource(R.string.add_expense_due_date_confirm)
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text(
                                        stringResource(R.string.add_expense_due_date_cancel)
                                    )
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }

                // ── Receipt Image ─────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.add_expense_receipt_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ReceiptImagePicker(
                        receiptUri = uiState.receiptUri,
                        onImageSelected = { uri ->
                            onEvent(AddExpenseUiEvent.ReceiptImageSelected(uri))
                        },
                        onRemoveImage = {
                            onEvent(AddExpenseUiEvent.RemoveReceiptImage)
                        }
                    )
                }
            }
        }
    }
}
