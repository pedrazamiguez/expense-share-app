package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event

sealed interface BalancesUiEvent {
    data object LoadBalances : BalancesUiEvent
    data class OnGroupSelected(val groupId: String) : BalancesUiEvent
}
