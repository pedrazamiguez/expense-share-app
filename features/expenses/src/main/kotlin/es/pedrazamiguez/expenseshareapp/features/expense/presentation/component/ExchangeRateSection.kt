package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Exchange rate section of the Add Expense form.
 * Shown when the selected currency differs from the group currency.
 * Contains the exchange rate input and the calculated group amount.
 *
 * When [AddExpenseUiState.isExchangeRateLocked] is true (CASH payment method),
 * both fields become read-only — the rate is derived from ATM withdrawal rates.
 */
@Composable
fun ExchangeRateSection(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = uiState.showExchangeRateSection,
        modifier = modifier
    ) {
        CurrencyConversionCard(
            exchangeRateValue = uiState.displayExchangeRate,
            exchangeRateLabel = uiState.exchangeRateLabel,
            groupAmountValue = uiState.calculatedGroupAmount,
            groupAmountLabel = uiState.groupAmountLabel,
            isLoadingRate = uiState.isLoadingRate,
            isExchangeRateLocked = uiState.isExchangeRateLocked,
            onExchangeRateChanged = { onEvent(AddExpenseUiEvent.ExchangeRateChanged(it)) },
            onGroupAmountChanged = { onEvent(AddExpenseUiEvent.GroupAmountChanged(it)) },
            focusManager = focusManager,
            cardStyle = CardStyle.STANDARD,
            exchangeRateLockedHint = uiState.exchangeRateLockedHint,
            isInsufficientCash = uiState.isInsufficientCash,
            isGroupAmountError = !uiState.isAmountValid
        )
    }
}
