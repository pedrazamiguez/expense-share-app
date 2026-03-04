package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event

sealed interface BalancesUiEvent {
    // Contribution events
    data object ShowAddMoneyDialog : BalancesUiEvent
    data object DismissAddMoneyDialog : BalancesUiEvent
    data class UpdateContributionAmount(val amount: String) : BalancesUiEvent
    data object SubmitContribution : BalancesUiEvent

    // Cash withdrawal events
    data object ShowWithdrawCashSheet : BalancesUiEvent
    data object DismissWithdrawCashSheet : BalancesUiEvent
    data class UpdateWithdrawalAmount(val amount: String) : BalancesUiEvent
    data class UpdateWithdrawalCurrency(val currency: String) : BalancesUiEvent
    data class UpdateWithdrawalDeducted(val deducted: String) : BalancesUiEvent
    data class UpdateWithdrawalExchangeRate(val rate: String) : BalancesUiEvent
    data object SubmitWithdrawal : BalancesUiEvent
}
