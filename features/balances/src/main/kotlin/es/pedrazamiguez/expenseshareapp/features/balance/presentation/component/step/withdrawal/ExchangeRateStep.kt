package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.withdrawal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyConversionCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyConversionCardState
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step 2: Exchange rate + deducted amount in group currency.
 * Only shown when a foreign currency is selected.
 */
@Composable
fun ExchangeRateStep(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                title = stringResource(R.string.withdrawal_exchange_rate_title),
                exchangeRateValue = uiState.displayExchangeRate,
                exchangeRateLabel = uiState.exchangeRateLabel,
                groupAmountValue = uiState.deductedAmount,
                groupAmountLabel = uiState.deductedAmountLabel,
                isLoadingRate = uiState.isLoadingRate,
                isExchangeRateLocked = false
            ),
            onExchangeRateChanged = { onEvent(AddCashWithdrawalUiEvent.ExchangeRateChanged(it)) },
            onGroupAmountChanged = { onEvent(AddCashWithdrawalUiEvent.DeductedAmountChanged(it)) }
        )
    }
}
