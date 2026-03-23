package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.component.WithdrawalAmountCurrencyCard
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step 1: Amount input + currency selector.
 * Always shown as the first wizard step.
 * The amount field is auto-focused so the keyboard opens immediately.
 */
@Composable
fun AmountStep(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WithdrawalAmountCurrencyCard(
            amount = uiState.withdrawalAmount,
            isAmountError = !uiState.isAmountValid,
            selectedCurrency = uiState.selectedCurrency,
            availableCurrencies = uiState.availableCurrencies,
            onAmountChanged = { onEvent(AddCashWithdrawalUiEvent.WithdrawalAmountChanged(it)) },
            onCurrencySelected = { onEvent(AddCashWithdrawalUiEvent.CurrencySelected(it)) },
            amountLabel = stringResource(R.string.balances_withdraw_cash_amount_hint),
            currencyLabel = stringResource(R.string.balances_withdraw_cash_currency_hint),
            autoFocus = true
        )
    }
}
