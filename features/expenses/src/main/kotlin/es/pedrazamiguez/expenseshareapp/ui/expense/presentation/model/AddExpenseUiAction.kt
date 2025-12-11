package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model

sealed interface AddExpenseUiAction {
    data object None : AddExpenseUiAction
    data class ShowError(val message: String) : AddExpenseUiAction
}
