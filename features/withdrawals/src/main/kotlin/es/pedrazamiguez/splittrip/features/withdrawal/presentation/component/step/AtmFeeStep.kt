package es.pedrazamiguez.splittrip.features.withdrawal.presentation.component.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency.AmountCurrencyCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency.AmountCurrencyCardState
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.withdrawal.R
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState

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
    onImeNext: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        AmountCurrencyCard(
            state = AmountCurrencyCardState(
                amount = uiState.feeAmount,
                isAmountError = !uiState.isFeeAmountValid,
                selectedCurrency = uiState.feeCurrency,
                availableCurrencies = uiState.availableCurrencies,
                amountLabel = stringResource(R.string.withdrawal_fee_amount_hint),
                currencyLabel = stringResource(R.string.withdrawal_fee_currency_hint),
                title = stringResource(R.string.withdrawal_fee_title),
                autoFocus = true
            ),
            onAmountChanged = { onEvent(AddCashWithdrawalUiEvent.FeeAmountChanged(it)) },
            onCurrencySelected = { onEvent(AddCashWithdrawalUiEvent.FeeCurrencySelected(it)) },
            onImeAction = onImeNext
        )
    }
}
