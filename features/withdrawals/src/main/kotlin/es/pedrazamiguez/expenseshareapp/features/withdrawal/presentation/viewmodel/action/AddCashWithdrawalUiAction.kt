package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface AddCashWithdrawalUiAction {
    data object None : AddCashWithdrawalUiAction
    data class ShowError(val message: UiText) : AddCashWithdrawalUiAction

    /** Signals the Feature to pop the back stack (Back pressed on the first wizard step). */
    data object NavigateBack : AddCashWithdrawalUiAction
}
