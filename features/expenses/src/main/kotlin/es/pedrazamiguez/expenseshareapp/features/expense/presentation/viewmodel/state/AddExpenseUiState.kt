package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import androidx.annotation.StringRes

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val expenseTitle: String = "",
    val expenseAmount: String = "",
    @field:StringRes
    val errorRes: Int? = null,
    val errorMessage: String? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true
)
