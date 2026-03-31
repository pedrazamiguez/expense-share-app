package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.withdrawal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCardLabels
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step 3 (after AMOUNT and optionally EXCHANGE_RATE): Scope selector.
 *
 * Lets the user choose who the cash withdrawal is for:
 *   • The whole group (GROUP)
 *   • A specific subunit (SUBUNIT)
 *   • Themselves (USER)
 */
@Composable
fun ScopeStep(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        PayerTypeScopeCard(
            labels = PayerTypeScopeCardLabels(
                title = stringResource(R.string.balances_withdraw_cash_withdrawing_for),
                groupLabel = stringResource(R.string.balances_withdraw_cash_for_group),
                personalLabel = stringResource(R.string.balances_withdraw_cash_for_me),
                subunitLabelTemplate = stringResource(R.string.balances_withdraw_cash_for_subunit)
            ),
            selectedScope = uiState.withdrawalScope,
            selectedSubunitId = uiState.selectedSubunitId,
            subunitOptions = uiState.subunitOptions,
            onScopeSelected = { scope, subunitId ->
                onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(scope, subunitId))
            }
        )
    }
}
