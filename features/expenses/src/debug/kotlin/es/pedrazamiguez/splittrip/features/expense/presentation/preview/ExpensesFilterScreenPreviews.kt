package es.pedrazamiguez.splittrip.features.expense.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.features.expense.presentation.screen.ExpensesFilterScreen
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpensesFilterUiState

@PreviewComplete
@Composable
private fun ExpensesFilterScreenDefaultPreview() {
    PreviewThemeWrapper {
        ExpensesFilterScreen(
            uiState = ExpensesFilterUiState(
                isLoading = false,
                groupId = "group-1",
                totalExpensesCount = 10,
                matchingExpensesCount = 10
            )
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesFilterScreenSingleCategoryPreview() {
    PreviewThemeWrapper {
        ExpensesFilterScreen(
            uiState = ExpensesFilterUiState(
                isLoading = false,
                groupId = "group-1",
                totalExpensesCount = 10,
                matchingExpensesCount = 4,
                draftCriteria = ExpenseFilterCriteria(
                    selectedCategories = setOf(ExpenseCategory.FOOD)
                )
            )
        )
    }
}

@PreviewComplete
@Composable
private fun ExpensesFilterScreenMultipleCategoriesPreview() {
    PreviewThemeWrapper {
        ExpensesFilterScreen(
            uiState = ExpensesFilterUiState(
                isLoading = false,
                groupId = "group-1",
                totalExpensesCount = 10,
                matchingExpensesCount = 2,
                draftCriteria = ExpenseFilterCriteria(
                    selectedCategories = setOf(ExpenseCategory.FOOD, ExpenseCategory.TRANSPORT),
                    selectedSubcategories = setOf(
                        ExpenseSubcategory.RESTAURANT,
                        ExpenseSubcategory.TAXI_RIDESHARE
                    )
                )
            )
        )
    }
}
