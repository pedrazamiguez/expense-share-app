package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
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

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        AddExpenseFormContent(
            uiState = uiState,
            onEvent = onEvent,
            focusManager = focusManager,
            amountFocusRequester = amountFocusRequester,
            modifier = Modifier.weight(1f)
        )

        SubmitButton(
            isFormValid = uiState.isFormValid,
            isLoading = uiState.isLoading,
            onSubmit = { submitForm() }
        )

        // Raw IME spacer — bypasses Scaffold inset consumption
        val density = LocalDensity.current
        val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
        Spacer(modifier = Modifier.height(imeBottomDp))
    }
}

@Composable
private fun AddExpenseFormContent(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    focusManager: FocusManager,
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
            focusRequester = amountFocusRequester,
            focusManager = focusManager
        )

        // Section 2: Exchange Rate (conditional)
        ExchangeRateSection(
            uiState = uiState,
            onEvent = onEvent,
            focusManager = focusManager
        )

        // Sections 3+4: Progressive Disclosure + Detail Fields
        ExpandableDetailsSection(
            uiState = uiState,
            onEvent = onEvent,
            focusManager = focusManager
        )

        // Section 5: Add-Ons (fees, tips, discounts, surcharges)
        AddOnsSection(
            uiState = uiState,
            onEvent = onEvent,
            focusManager = focusManager
        )

        // Error Banner
        FormErrorBanner(error = uiState.error)
    }
}

@Composable
private fun FormErrorBanner(error: UiText?) {
    error?.let { errorUiText ->
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = errorUiText.asString(),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
