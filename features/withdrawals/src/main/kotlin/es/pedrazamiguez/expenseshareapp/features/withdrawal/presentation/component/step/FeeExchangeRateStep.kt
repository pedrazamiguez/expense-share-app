package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.component.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyConversionCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyConversionCardState
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.withdrawal.R
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState

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
    WizardStepLayout(modifier = modifier) {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                title = stringResource(R.string.withdrawal_fee_exchange_rate_title),
                exchangeRateValue = uiState.feeExchangeRate,
                exchangeRateLabel = uiState.feeExchangeRateLabel,
                groupAmountValue = uiState.feeConvertedAmount,
                groupAmountLabel = uiState.feeConvertedLabel,
                isLoadingRate = false,
                isExchangeRateLocked = false,
                autoFocus = true
            ),
            onExchangeRateChanged = { onEvent(AddCashWithdrawalUiEvent.FeeExchangeRateChanged(it)) },
            onGroupAmountChanged = { onEvent(AddCashWithdrawalUiEvent.FeeConvertedAmountChanged(it)) }
        )
    }
}
