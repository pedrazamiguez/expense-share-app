package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.SplitSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 5: Split type + per-member allocation + sub-unit mode.
 * Only shown when the group has more than one member.
 */
@Composable
fun SplitStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        SplitSection(uiState = uiState, onEvent = onEvent)
    }
}
