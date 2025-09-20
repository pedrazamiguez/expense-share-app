package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model

sealed interface BalanceUiAction {
    data class NavigateToGroup(val groupId: String) : BalanceUiAction
    data class ShowError(val message: String) : BalanceUiAction
}
