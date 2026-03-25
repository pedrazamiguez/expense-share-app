package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormErrorBanner
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormSubmitButton
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

@Composable
fun AddExpenseForm(
    groupId: String?,
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val amountFocusRequester = remember { FocusRequester() }

    val submitForm = {
        focusManager.clearFocus()
        if (uiState.isFormValid && !uiState.isLoading) {
            onEvent(AddExpenseUiEvent.SubmitAddExpense(groupId))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AddExpenseFormContent(
                uiState = uiState,
                onEvent = onEvent,
                amountFocusRequester = amountFocusRequester,
                modifier = Modifier.weight(1f)
            )

            FormSubmitButton(
                label = stringResource(R.string.add_expense_submit_button),
                isEnabled = uiState.isFormValid,
                isLoading = uiState.isLoading,
                onSubmit = { submitForm() }
            )
        }
    }
}

@Composable
private fun AddExpenseFormContent(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    amountFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section 1: Quick Add — Amount + Currency + Title
        QuickAddSection(
            uiState = uiState,
            onEvent = onEvent,
            focusRequester = amountFocusRequester
        )

        // Section 2: Exchange Rate (conditional)
        ExchangeRateSection(
            uiState = uiState,
            onEvent = onEvent
        )

        // Sections 3+4: Progressive Disclosure + Detail Fields
        ExpandableDetailsSection(
            uiState = uiState,
            onEvent = onEvent
        )

        // Section 5: Add-Ons (fees, tips, discounts, surcharges)
        AddOnsSection(
            uiState = uiState,
            onEvent = onEvent
        )

        // Error Banner
        FormErrorBanner(error = uiState.error)
    }
}
