package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyDropdown
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/** Weight ratio for amount input field vs currency dropdown. */
private const val AMOUNT_FIELD_WEIGHT = 0.55f
private const val CURRENCY_FIELD_WEIGHT = 0.45f

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
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            StyledOutlinedTextField(
                value = uiState.sourceAmount,
                onValueChange = { onEvent(AddExpenseUiEvent.SourceAmountChanged(it)) },
                label = stringResource(R.string.add_expense_amount_paid),
                modifier = Modifier.weight(AMOUNT_FIELD_WEIGHT),
                keyboardType = KeyboardType.Decimal,
                isError = !uiState.isAmountValid,
                imeAction = ImeAction.Next,
                focusRequester = focusRequester
            )
            CurrencyDropdown(
                selectedCurrency = uiState.selectedCurrency,
                availableCurrencies = uiState.availableCurrencies,
                onCurrencySelected = { onEvent(AddExpenseUiEvent.CurrencySelected(it)) },
                label = stringResource(R.string.add_expense_currency_label),
                modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
            )
        }

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
