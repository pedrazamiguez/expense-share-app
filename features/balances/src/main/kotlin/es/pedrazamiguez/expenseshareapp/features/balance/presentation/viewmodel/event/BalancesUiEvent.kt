package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event

sealed interface BalancesUiEvent {
    // Contribution events
    data object ShowAddMoneyDialog : BalancesUiEvent
    data object DismissAddMoneyDialog : BalancesUiEvent
    data class UpdateContributionAmount(val amount: String) : BalancesUiEvent
    data object SubmitContribution : BalancesUiEvent
}
