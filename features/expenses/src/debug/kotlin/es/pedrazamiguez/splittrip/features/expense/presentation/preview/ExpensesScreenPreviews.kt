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
        val count = expenseGroups.sumOf { it.expenses.size }
        ExpensesScreen(
            uiState = ExpensesUiState(
                isLoading = false,
                groupId = "group-1",
                expenseGroups = expenseGroups,
                totalExpensesCount = count,
                formattedTotalSpent = "€125.50",
                visibleExpensesCount = count,
                isFiltered = false
            )
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesScreenSearchActivePreview() {
    ExpenseListPreviewHelper { expenseGroups ->
        val count = expenseGroups.sumOf { it.expenses.size }
        ExpensesScreen(
            uiState = ExpensesUiState(
                isLoading = false,
                groupId = "group-1",
                searchQuery = "Dinner",
                expenseGroups = expenseGroups,
                totalExpensesCount = count,
                formattedTotalSpent = "€50.00",
                visibleExpensesCount = count,
                isFiltered = true
            )
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesScreenEmptySearchPreview() {
    PreviewThemeWrapper {
        ExpensesScreen(
            uiState = ExpensesUiState(
                isLoading = false,
                groupId = "group-1",
                searchQuery = "Nonexistent",
                totalExpensesCount = 5,
                formattedTotalSpent = "€0.00",
                visibleExpensesCount = 0,
                isFiltered = true
            )
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesScreenWithScheduledDataPreview() {
    ExpenseListPreviewHelper { expenseGroups ->
        val count = expenseGroups.sumOf { it.expenses.size }
        ExpensesScreen(
            uiState = ExpensesUiState(
                isLoading = false,
                groupId = "group-1",
                expenseGroups = expenseGroups,
                totalExpensesCount = count,
                formattedTotalSpent = "€125.50",
                formattedTotalScheduled = "€234.00",
                visibleExpensesCount = count,
                isFiltered = false
            )
        )
    }
}
