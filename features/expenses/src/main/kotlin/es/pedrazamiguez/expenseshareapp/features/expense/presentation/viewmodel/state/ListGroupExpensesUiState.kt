package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel

data class ListGroupExpensesUiState(
    val expenses: List<ExpenseUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
