package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface AddCashWithdrawalUiAction {
    data object None : AddCashWithdrawalUiAction
    data class ShowError(val message: UiText) : AddCashWithdrawalUiAction
}

