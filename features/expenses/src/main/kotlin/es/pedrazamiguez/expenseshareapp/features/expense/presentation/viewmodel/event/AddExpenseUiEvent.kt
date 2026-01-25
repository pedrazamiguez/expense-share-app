package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event

sealed interface AddExpenseUiEvent {
    data class TitleChanged(val title: String) : AddExpenseUiEvent
    data class AmountChanged(val amount: String) : AddExpenseUiEvent
    data class SubmitAddExpense(val groupId: String?) : AddExpenseUiEvent
}
