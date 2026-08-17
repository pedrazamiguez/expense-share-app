package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.event

import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria

sealed interface ExpensesUiEvent {
    data object LoadExpenses : ExpensesUiEvent
    data class ScrollPositionChanged(val index: Int, val offset: Int) : ExpensesUiEvent
    data class DeleteExpense(val expenseId: String) : ExpensesUiEvent
    data class CancelExpense(val expenseId: String) : ExpensesUiEvent
    data object ExpenseAdded : ExpensesUiEvent
    data class SearchQueryChanged(val query: String) : ExpensesUiEvent
    data class FilterCriteriaChanged(val criteria: ExpenseFilterCriteria) : ExpensesUiEvent
    data object ClearFilters : ExpensesUiEvent
}
