package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyConversionCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyConversionCardState
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 2: Exchange rate + calculated group amount.
 * Only shown when a foreign currency is selected.
 */
@Composable
fun ExchangeRateStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                title = stringResource(R.string.add_expense_exchange_rate_title),
                exchangeRateValue = uiState.displayExchangeRate,
                exchangeRateLabel = uiState.exchangeRateLabel,
                groupAmountValue = uiState.calculatedGroupAmount,
                groupAmountLabel = uiState.groupAmountLabel,
                isLoadingRate = uiState.isLoadingRate,
                isExchangeRateLocked = uiState.isExchangeRateLocked,
                exchangeRateLockedHint = uiState.exchangeRateLockedHint,
                isInsufficientCash = uiState.isInsufficientCash,
                isGroupAmountError = !uiState.isAmountValid
            ),
            onExchangeRateChanged = { onEvent(AddExpenseUiEvent.ExchangeRateChanged(it)) },
            onGroupAmountChanged = { onEvent(AddExpenseUiEvent.GroupAmountChanged(it)) }
        )
    }
}
