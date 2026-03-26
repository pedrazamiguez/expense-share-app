package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.VendorNotesSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 7: Vendor name and optional notes.
 */
@Composable
fun VendorNotesStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        VendorNotesSection(
            vendor = uiState.vendor,
            notes = uiState.notes,
            onVendorChanged = { onEvent(AddExpenseUiEvent.VendorChanged(it)) },
            onNotesChanged = { onEvent(AddExpenseUiEvent.NotesChanged(it)) }
        )
    }
}
