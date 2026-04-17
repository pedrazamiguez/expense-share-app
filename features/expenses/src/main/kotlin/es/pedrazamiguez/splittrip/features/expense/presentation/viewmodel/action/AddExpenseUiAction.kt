package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface AddExpenseUiAction {
    data object None : AddExpenseUiAction
    data class ShowError(val message: UiText) : AddExpenseUiAction
    data object NavigateBack : AddExpenseUiAction

    /**
     * Emitted when a cash-tranche conflict is detected at save time (i.e. another group
     * member consumed cash between the preview and the user's submit). The Feature handles
     * this by showing the conflict-specific [message] via the top pill notification and
     * refreshing the tranche preview with the latest Room data.
     */
    data class ShowCashConflictError(val message: UiText) : AddExpenseUiAction
}
