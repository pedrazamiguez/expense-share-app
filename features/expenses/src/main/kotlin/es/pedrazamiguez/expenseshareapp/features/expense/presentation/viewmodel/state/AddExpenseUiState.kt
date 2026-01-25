package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val expenseTitle: String = "",
    val expenseAmount: String = "",
    val error: String? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true
)
