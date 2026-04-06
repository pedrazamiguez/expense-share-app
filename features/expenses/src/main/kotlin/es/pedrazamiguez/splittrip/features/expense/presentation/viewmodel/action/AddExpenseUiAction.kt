package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface AddExpenseUiAction {
    data object None : AddExpenseUiAction
    data class ShowError(val message: UiText) : AddExpenseUiAction
    data object NavigateBack : AddExpenseUiAction
}
