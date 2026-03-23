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
 * Step ATM_FEE: fee amount + fee currency.
 *
 * Only shown when the user has toggled the ATM fee on in the preceding [DetailsStep].
 * The optional fee conversion card has been promoted to its own [FeeExchangeRateStep]
 * so the UX is symmetric with the withdrawal amount → [ExchangeRateStep] pattern.
 */
@Composable
fun AtmFeeStep(
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
            amount = uiState.feeAmount,
            isAmountError = !uiState.isFeeAmountValid,
            selectedCurrency = uiState.feeCurrency,
            availableCurrencies = uiState.availableCurrencies,
            onAmountChanged = { onEvent(AddCashWithdrawalUiEvent.FeeAmountChanged(it)) },
            onCurrencySelected = { onEvent(AddCashWithdrawalUiEvent.FeeCurrencySelected(it)) },
            amountLabel = stringResource(R.string.withdrawal_fee_amount_hint),
            currencyLabel = stringResource(R.string.withdrawal_fee_currency_hint),
            title = stringResource(R.string.withdrawal_fee_title),
            autoFocus = true
        )
    }
}
