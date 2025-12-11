package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val expenseTitle: String = "",
    val expenseAmount: String = "",
    val error: String? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true
)
