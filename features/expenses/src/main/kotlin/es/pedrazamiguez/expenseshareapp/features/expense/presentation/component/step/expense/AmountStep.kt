package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyDropdown
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/** Weight ratio for amount input field vs currency dropdown. */
private const val AMOUNT_FIELD_WEIGHT = 0.55f
private const val CURRENCY_FIELD_WEIGHT = 0.45f

/**
 * Step 3: Amount + Currency.
 * The amount field is auto-focused so the keyboard opens immediately.
 */
@Composable
fun AmountStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val amountFocusRequester = remember { FocusRequester() }
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            amountFocusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    WizardStepLayout(modifier = modifier) {
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
                imeAction = ImeAction.Done,
                focusRequester = amountFocusRequester
            )
            CurrencyDropdown(
                selectedCurrency = uiState.selectedCurrency,
                availableCurrencies = uiState.availableCurrencies,
                onCurrencySelected = { onEvent(AddExpenseUiEvent.CurrencySelected(it)) },
                label = stringResource(R.string.add_expense_currency_label),
                modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
            )
        }
    }
}
