package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event

sealed interface ExpensesUiEvent {
    data object LoadExpenses : ExpensesUiEvent
    data class ScrollPositionChanged(val index: Int, val offset: Int) : ExpensesUiEvent
}
