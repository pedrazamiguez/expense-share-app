package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step

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
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step FEE_EXCHANGE_RATE: ATM fee exchange rate + converted amount in group currency.
 *
 * Only shown when the fee currency differs from the group currency, mirroring the
 * [ExchangeRateStep] pattern used for the withdrawal amount.
 */
@Composable
fun FeeExchangeRateStep(
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
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                title = stringResource(R.string.withdrawal_fee_exchange_rate_title),
                exchangeRateValue = uiState.feeExchangeRate,
                exchangeRateLabel = uiState.feeExchangeRateLabel,
                groupAmountValue = uiState.feeConvertedAmount,
                groupAmountLabel = uiState.feeConvertedLabel,
                isLoadingRate = false,
                isExchangeRateLocked = false
            ),
            onExchangeRateChanged = { onEvent(AddCashWithdrawalUiEvent.FeeExchangeRateChanged(it)) },
            onGroupAmountChanged = { onEvent(AddCashWithdrawalUiEvent.FeeConvertedAmountChanged(it)) }
        )
    }
}
