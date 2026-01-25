package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

data class ExpenseUiModel(
    val id: String = "",
    val title: String = "",
    val formattedAmount: String = "",
    val paidByText: String = "",
    val dateText: String = ""
)
