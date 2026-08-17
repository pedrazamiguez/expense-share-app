package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria

sealed interface ExpensesFilterUiAction {
    data class ApplyAndNavigateBack(val appliedCriteria: ExpenseFilterCriteria) : ExpensesFilterUiAction
    data class FiltersReset(val clearedCriteria: ExpenseFilterCriteria) : ExpensesFilterUiAction
    data object NavigateBack : ExpensesFilterUiAction
}
