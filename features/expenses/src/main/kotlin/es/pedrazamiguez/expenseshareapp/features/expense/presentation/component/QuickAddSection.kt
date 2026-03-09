package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Quick Add section of the Add Expense form.
 * Contains the Amount + Currency row and the Title field.
 * Auto-focuses the amount field when first composed.
 */
@Composable
fun QuickAddSection(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    // Auto-focus the amount field when the form appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                imeAction = ImeAction.Next,
                focusRequester = focusRequester
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
}

