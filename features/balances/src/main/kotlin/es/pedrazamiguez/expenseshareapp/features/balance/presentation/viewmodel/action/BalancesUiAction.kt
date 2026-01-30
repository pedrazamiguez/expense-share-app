package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action

sealed interface BalancesUiAction {
    data class NavigateToGroup(val groupId: String) : BalancesUiAction
    data class ShowError(val message: String) : BalancesUiAction
}
