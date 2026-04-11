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
 * Step 1: Amount input + currency selector.
 * Always shown as the first wizard step.
 * The amount field is auto-focused so the keyboard opens immediately.
 */
@Composable
fun AmountStep(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    onImeNext: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        AmountCurrencyCard(
            state = AmountCurrencyCardState(
                amount = uiState.withdrawalAmount,
                isAmountError = !uiState.isAmountValid,
                selectedCurrency = uiState.selectedCurrency,
                availableCurrencies = uiState.availableCurrencies,
                amountLabel = stringResource(R.string.withdrawal_cash_amount_hint),
                currencyLabel = stringResource(R.string.withdrawal_cash_currency_hint),
                autoFocus = true
            ),
            onAmountChanged = { onEvent(AddCashWithdrawalUiEvent.WithdrawalAmountChanged(it)) },
            onCurrencySelected = { onEvent(AddCashWithdrawalUiEvent.CurrencySelected(it)) },
            onImeAction = onImeNext
        )
    }
}
