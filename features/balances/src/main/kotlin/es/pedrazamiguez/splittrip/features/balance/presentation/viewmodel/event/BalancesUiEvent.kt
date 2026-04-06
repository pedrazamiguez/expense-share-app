package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.event

sealed interface BalancesUiEvent {

    // Balance animation
    data object BalanceAnimationComplete : BalancesUiEvent
}
