package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

sealed interface AddExpenseUiAction {
    data object None : AddExpenseUiAction
    data class ShowError(val message: String) : AddExpenseUiAction
}
