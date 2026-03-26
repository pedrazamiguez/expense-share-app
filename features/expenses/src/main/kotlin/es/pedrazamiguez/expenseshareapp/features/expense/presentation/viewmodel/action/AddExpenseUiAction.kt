package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface AddExpenseUiAction {
    data object None : AddExpenseUiAction
    data class ShowError(val message: UiText) : AddExpenseUiAction
    data object NavigateBack : AddExpenseUiAction
}
