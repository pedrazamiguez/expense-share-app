package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.component.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.MemberPickerCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.MemberPickerCardLabels
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCardLabels
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.withdrawal.R
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step 3 (after AMOUNT and optionally EXCHANGE_RATE): Scope selector.
 *
 * Lets the user choose:
 *   • Which group member is withdrawing (member picker — impersonation)
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
        MemberPickerCard(
            labels = MemberPickerCardLabels(
                title = stringResource(R.string.withdrawal_member_picker_title),
                currentUserSuffix = stringResource(R.string.withdrawal_member_picker_you_suffix)
            ),
            members = uiState.groupMembers,
            selectedMemberId = uiState.selectedMemberId,
            onMemberSelected = { userId ->
                onEvent(AddCashWithdrawalUiEvent.MemberSelected(userId))
            }
        )

        PayerTypeScopeCard(
            labels = PayerTypeScopeCardLabels(
                title = stringResource(R.string.withdrawal_cash_withdrawing_for),
                groupLabel = stringResource(R.string.withdrawal_cash_for_group),
                personalLabel = stringResource(R.string.withdrawal_cash_for_me),
                subunitLabelTemplate = stringResource(R.string.withdrawal_cash_for_subunit)
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
