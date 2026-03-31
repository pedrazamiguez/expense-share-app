package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.contribution

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.PayerTypeScopeCardLabels
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState

/**
 * Step 2: Scope selector — who the contribution is for.
 *
 * Lets the user choose:
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
        PayerTypeScopeCard(
            labels = PayerTypeScopeCardLabels(
                title = stringResource(R.string.balances_add_money_contributing_for),
                groupLabel = stringResource(R.string.balances_add_money_for_group),
                personalLabel = stringResource(R.string.balances_add_money_for_me),
                subunitLabelTemplate = stringResource(R.string.balances_add_money_for_subunit)
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
