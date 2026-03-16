package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event

sealed interface BalancesUiEvent {

    // Balance animation
    data object BalanceAnimationComplete : BalancesUiEvent
}
