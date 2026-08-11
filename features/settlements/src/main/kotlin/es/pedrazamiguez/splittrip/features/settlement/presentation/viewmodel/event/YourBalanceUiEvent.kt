package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event

sealed class YourBalanceUiEvent {
    data object Refresh : YourBalanceUiEvent()
    data object ShowCashBreakdown : YourBalanceUiEvent()
    data object DismissCashBreakdown : YourBalanceUiEvent()
    data class ConfirmSettlement(val settlementId: String) : YourBalanceUiEvent()
    data class DisputeSettlement(val settlementId: String) : YourBalanceUiEvent()
    data class DisputeReasonChanged(val reason: String) : YourBalanceUiEvent()
    data object DisputeSubmitted : YourBalanceUiEvent()
    data object DisputeCancelled : YourBalanceUiEvent()
    data class NudgeDebtor(val settlementId: String) : YourBalanceUiEvent()
    data class ChartModeToggled(val cashOnly: Boolean) : YourBalanceUiEvent()
}
