package es.pedrazamiguez.expenseshareapp.features.contribution.presentation.component.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.MemberPickerCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.MemberPickerCardLabels
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCardLabels
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.contribution.R
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.viewmodel.state.AddContributionUiState

/**
 * Step 2: Scope selector — who the contribution is for.
 *
 * Lets the user choose:
 *   • Which group member is contributing (member picker — impersonation)
 *   • The whole group (GROUP)
 *   • A specific subunit (SUBUNIT)
 *   • Themselves (USER)
 */
@Composable
fun ContributionScopeStep(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        MemberPickerCard(
            labels = MemberPickerCardLabels(
                title = stringResource(R.string.contribution_member_picker_title),
                currentUserSuffix = stringResource(R.string.contribution_member_picker_you_suffix)
            ),
            members = uiState.groupMembers,
            selectedMemberId = uiState.selectedMemberId,
            onMemberSelected = { userId ->
                onEvent(AddContributionUiEvent.MemberSelected(userId))
            }
        )

        PayerTypeScopeCard(
            labels = PayerTypeScopeCardLabels(
                title = stringResource(R.string.contribution_add_money_contributing_for),
                groupLabel = stringResource(R.string.contribution_add_money_for_group),
                personalLabel = stringResource(R.string.contribution_add_money_for_me),
                subunitLabelTemplate = stringResource(R.string.contribution_add_money_for_subunit)
            ),
            selectedScope = uiState.contributionScope,
            selectedSubunitId = uiState.selectedSubunitId,
            subunitOptions = uiState.subunitOptions,
            onScopeSelected = { scope, subunitId ->
                onEvent(AddContributionUiEvent.ContributionScopeSelected(scope, subunitId))
            }
        )
    }
}
