package es.pedrazamiguez.splittrip.features.expense.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.features.expense.presentation.screen.ExpensesScreen
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpensesUiState

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
