package es.pedrazamiguez.splittrip.features.expense.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberOptionUiModel
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.features.expense.presentation.screen.ExpensesFilterScreen
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpensesFilterUiState
import kotlinx.collections.immutable.persistentListOf

private val sampleMembers = persistentListOf(
    MemberOptionUiModel(userId = "user-1", displayName = "You", isCurrentUser = true),
    MemberOptionUiModel(userId = "user-2", displayName = "Ana", isCurrentUser = false),
    MemberOptionUiModel(userId = "user-3", displayName = "Carlos", isCurrentUser = false)
)

@PreviewComplete
@Composable
private fun ExpensesFilterScreenDefaultPreview() {
    PreviewThemeWrapper {
        ExpensesFilterScreen(
            uiState = ExpensesFilterUiState(
                isLoading = false,
                groupId = "group-1",
                totalExpensesCount = 10,
                matchingExpensesCount = 10,
                availableMembers = sampleMembers
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
                availableMembers = sampleMembers,
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
                availableMembers = sampleMembers,
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

@PreviewComplete
@Composable
private fun ExpensesFilterScreenMemberFilterPreview() {
    PreviewThemeWrapper {
        ExpensesFilterScreen(
            uiState = ExpensesFilterUiState(
                isLoading = false,
                groupId = "group-1",
                totalExpensesCount = 10,
                matchingExpensesCount = 3,
                availableMembers = sampleMembers,
                draftCriteria = ExpenseFilterCriteria(
                    selectedMemberIds = setOf("user-1", "user-2")
                )
            )
        )
    }
}
