package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.R
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
    val focusManager = LocalFocusManager.current

    WizardStepLayout(modifier = modifier) {
        SectionCard(title = stringResource(R.string.add_expense_vendor_helper)) {
            StyledOutlinedTextField(
                value = uiState.vendor,
                onValueChange = { onEvent(AddExpenseUiEvent.VendorChanged(it)) },
                label = stringResource(R.string.add_expense_vendor_label),
                modifier = Modifier.fillMaxWidth(),
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        }

        SectionCard(title = stringResource(R.string.add_expense_notes_helper)) {
            StyledOutlinedTextField(
                value = uiState.notes,
                onValueChange = { onEvent(AddExpenseUiEvent.NotesChanged(it)) },
                label = stringResource(R.string.add_expense_notes_label),
                modifier = Modifier.fillMaxWidth(),
                capitalization = KeyboardCapitalization.Sentences,
                singleLine = false,
                maxLines = 5,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}
