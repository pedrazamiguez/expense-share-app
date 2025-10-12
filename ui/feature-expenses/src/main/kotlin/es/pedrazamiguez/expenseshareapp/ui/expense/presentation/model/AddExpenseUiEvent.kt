package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model

sealed interface AddExpenseUiEvent {
    data class TitleChanged(val title: String) : AddExpenseUiEvent
    data class AmountChanged(val amount: String) : AddExpenseUiEvent
    data class SubmitAddExpense(val groupId: String?) : AddExpenseUiEvent
}
