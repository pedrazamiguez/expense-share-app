package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

sealed interface BalanceUiAction {
    data object None : BalanceUiAction
    data class NavigateToGroup(val groupId: String) : BalanceUiAction
    data class ShowError(val message: String) : BalanceUiAction
}
