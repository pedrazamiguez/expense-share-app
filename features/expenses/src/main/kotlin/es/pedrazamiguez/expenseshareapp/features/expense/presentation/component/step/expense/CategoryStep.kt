package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.CategorySection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 6: Category selection.
 */
@Composable
fun CategoryStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        CategorySection(
            availableCategories = uiState.availableCategories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { onEvent(AddExpenseUiEvent.CategorySelected(it)) }
        )
    }
}
