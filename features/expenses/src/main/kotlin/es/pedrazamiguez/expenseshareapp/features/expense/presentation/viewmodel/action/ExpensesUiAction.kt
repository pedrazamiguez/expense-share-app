package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface ExpensesUiAction {
    data class ShowDeleteSuccess(val message: UiText) : ExpensesUiAction
    data class ShowDeleteError(val message: UiText) : ExpensesUiAction
}
