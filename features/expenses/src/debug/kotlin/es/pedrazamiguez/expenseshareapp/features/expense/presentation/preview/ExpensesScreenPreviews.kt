package es.pedrazamiguez.expenseshareapp.features.expense.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.ExpensesScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.ExpensesUiState

@PreviewComplete
@Composable
private fun ExpensesScreenLoadingPreview() {
    PreviewThemeWrapper {
        ExpensesScreen(
            uiState = ExpensesUiState(isLoading = true)
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesScreenEmptyPreview() {
    PreviewThemeWrapper {
        ExpensesScreen(
            uiState = ExpensesUiState(isLoading = false, groupId = "group-1")
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesScreenWithDataPreview() {
    ExpenseListPreviewHelper { expenseGroups ->
        ExpensesScreen(
            uiState = ExpensesUiState(
                isLoading = false,
                groupId = "group-1",
                expenseGroups = expenseGroups
            )
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesScreenErrorPreview() {
    PreviewThemeWrapper {
        ExpensesScreen(
            uiState = ExpensesUiState(
                isLoading = false,
                errorMessage = "Failed to load expenses. Please try again."
            )
        )
    }
}
