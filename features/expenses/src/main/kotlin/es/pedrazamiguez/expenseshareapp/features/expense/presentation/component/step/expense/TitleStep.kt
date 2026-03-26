package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 1: Expense title.
 * Auto-focused so the keyboard opens immediately.
 */
@Composable
fun TitleStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            titleFocusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    WizardStepLayout(modifier = modifier) {
        StyledOutlinedTextField(
            value = uiState.expenseTitle,
            onValueChange = { onEvent(AddExpenseUiEvent.TitleChanged(it)) },
            label = stringResource(R.string.add_expense_what_for),
            modifier = Modifier.fillMaxWidth(),
            isError = !uiState.isTitleValid,
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            focusRequester = titleFocusRequester
        )
    }
}
