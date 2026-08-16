package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.expense.presentation.model.ExpenseDateGroupUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ExpensesUiState(
    val expenseGroups: ImmutableList<ExpenseDateGroupUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0,
    val groupId: String? = null,
    val isGroupArchived: Boolean = false,
    val searchQuery: String = "",
    val totalExpensesCount: Int = 0
) {
    /** True when the group has no expenses at all. */
    val isGroupEmpty: Boolean
        get() = totalExpensesCount == 0

    /** True when search query is active but no expenses match the query. */
    val isSearchResultEmpty: Boolean
        get() = searchQuery.isNotBlank() && totalExpensesCount > 0 && expenseGroups.all { it.expenses.isEmpty() }

    /** True when there are no expenses across all date groups. */
    val isEmpty: Boolean
        get() = expenseGroups.all { it.expenses.isEmpty() }
}
