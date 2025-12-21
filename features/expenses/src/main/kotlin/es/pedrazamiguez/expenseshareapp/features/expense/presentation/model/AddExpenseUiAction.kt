package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

import androidx.annotation.StringRes

sealed interface AddExpenseUiAction {
    data object None : AddExpenseUiAction
    data class ShowError(
        @param:StringRes
        val messageRes: Int? = null, val message: String? = null
    ) : AddExpenseUiAction
}
