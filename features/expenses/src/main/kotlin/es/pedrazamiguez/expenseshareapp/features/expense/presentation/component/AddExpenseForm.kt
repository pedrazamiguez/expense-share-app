package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseForm(
    groupId: String?,
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showDetails by rememberSaveable { mutableStateOf(false) }

    val submitForm = {
        focusManager.clearFocus()
        if (uiState.isFormValid && !uiState.isLoading) {
            onEvent(AddExpenseUiEvent.SubmitAddExpense(groupId))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // ── Scrollable content area ─────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ══════════════════════════════════════════════════════════
            // SECTION 1: Quick Add — Amount + Title (Expressive Header)
            // ══════════════════════════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // ── Amount + Currency (Hero input) ────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    StyledOutlinedTextField(
                        value = uiState.sourceAmount,
                        onValueChange = { onEvent(AddExpenseUiEvent.SourceAmountChanged(it)) },
                        label = stringResource(R.string.add_expense_amount_paid),
                        modifier = Modifier.weight(0.55f),
                        keyboardType = KeyboardType.Decimal,
                        isError = !uiState.isAmountValid,
                        imeAction = ImeAction.Next
                    )

                    Box(modifier = Modifier.weight(0.45f)) {
                        var expanded by remember { mutableStateOf(false) }
                        StyledOutlinedTextField(
                            value = uiState.selectedCurrency?.displayText ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = stringResource(R.string.add_expense_currency_label),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.availableCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency.displayText) },
                                    onClick = {
                                        onEvent(AddExpenseUiEvent.CurrencySelected(currency.code))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Title (What for?) ─────────────────────────────────
                StyledOutlinedTextField(
                    value = uiState.expenseTitle,
                    onValueChange = { onEvent(AddExpenseUiEvent.TitleChanged(it)) },
                    label = stringResource(R.string.add_expense_what_for),
                    modifier = Modifier.fillMaxWidth(),
                    isError = !uiState.isTitleValid,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            // ══════════════════════════════════════════════════════════
            // SECTION 2: Exchange Rate (conditional — always visible)
            // ══════════════════════════════════════════════════════════
            AnimatedVisibility(visible = uiState.showExchangeRateSection) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.add_expense_exchange_rate_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.isLoadingRate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StyledOutlinedTextField(
                                value = uiState.displayExchangeRate,
                                onValueChange = {
                                    onEvent(AddExpenseUiEvent.ExchangeRateChanged(it))
                                },
                                label = uiState.exchangeRateLabel,
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )

                            StyledOutlinedTextField(
                                value = uiState.calculatedGroupAmount,
                                onValueChange = {
                                    onEvent(AddExpenseUiEvent.GroupAmountChanged(it))
                                },
                                label = uiState.groupAmountLabel,
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Decimal,
                                isError = !uiState.isAmountValid,
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════
            // SECTION 3: Progressive Disclosure Toggle
            // ══════════════════════════════════════════════════════════
            TextButton(
                onClick = { showDetails = !showDetails },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showDetails) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = if (showDetails) stringResource(R.string.add_expense_less_details)
                    else stringResource(R.string.add_expense_more_details),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ══════════════════════════════════════════════════════════
            // SECTION 4: Expandable Detail Sections
            // ══════════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    // ── Split Section ─────────────────────────────────
                    if (uiState.availableSplitTypes.isNotEmpty() &&
                        uiState.memberIds.size > 1
                    ) {
                        SplitSection(
                            uiState = uiState,
                            onEvent = onEvent
                        )
                    }

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

            // ── Error Banner ──────────────────────────────────────────
            uiState.error?.let { errorUiText ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorUiText.asString(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════════
        // PINNED SUBMIT BUTTON — always visible above keyboard
        // ══════════════════════════════════════════════════════════════
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { submitForm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(56.dp),
                enabled = uiState.isFormValid && !uiState.isLoading,
                shape = MaterialTheme.shapes.large
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.add_expense_submit_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
