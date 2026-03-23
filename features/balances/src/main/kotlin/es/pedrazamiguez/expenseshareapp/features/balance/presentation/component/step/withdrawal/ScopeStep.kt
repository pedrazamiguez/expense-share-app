package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.withdrawal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.PayerTypeScopeCard
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.PayerTypeScopeCardLabels
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step 3 (after AMOUNT and optionally EXCHANGE_RATE): Scope selector.
 *
 * Lets the user choose who the cash withdrawal is for:
 *   • The whole group (GROUP)
 *   • A specific sub-unit (SUBUNIT)
 *   • Themselves (USER)
 */
@Composable
fun ScopeStep(
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
