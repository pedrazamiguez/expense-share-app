package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel

sealed interface ListGroupExpensesUiState {
    data object Idle : ListGroupExpensesUiState
    data object Loading : ListGroupExpensesUiState
    data class Success(val expenses: List<ExpenseUiModel>) : ListGroupExpensesUiState
    data class Error(val message: String) : ListGroupExpensesUiState
}