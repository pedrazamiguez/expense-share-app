package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.AmountCurrencyCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.AmountCurrencyCardState
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 3: Amount + Currency.
 * Uses the shared [AmountCurrencyCard] component for consistency
 * with other wizard flows (e.g. cash-withdrawal, contributions).
 */
@Composable
fun AmountStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        AmountCurrencyCard(
            state = AmountCurrencyCardState(
                amount = uiState.sourceAmount,
                isAmountError = !uiState.isAmountValid,
                selectedCurrency = uiState.selectedCurrency,
                availableCurrencies = uiState.availableCurrencies,
                amountLabel = stringResource(R.string.add_expense_amount_paid),
                currencyLabel = stringResource(R.string.add_expense_currency_label),
                autoFocus = true
            ),
            onAmountChanged = { onEvent(AddExpenseUiEvent.SourceAmountChanged(it)) },
            onCurrencySelected = { onEvent(AddExpenseUiEvent.CurrencySelected(it)) }
        )
    }
}
