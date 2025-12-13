package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

sealed interface BalanceUiEvent {
    data object Refresh : BalanceUiEvent
    data class OnGroupSelected(val groupId: String) : BalanceUiEvent
}
