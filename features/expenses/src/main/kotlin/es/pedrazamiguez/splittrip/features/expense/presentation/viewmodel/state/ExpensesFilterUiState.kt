package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria

data class ExpensesFilterUiState(
    val draftCriteria: ExpenseFilterCriteria = ExpenseFilterCriteria(),
    val matchingExpensesCount: Int = 0,
    val totalExpensesCount: Int = 0,
    val isLoading: Boolean = false,
    val groupId: String? = null
) {
    val canReset: Boolean
        get() = draftCriteria.activeFilterCount > 0
}
