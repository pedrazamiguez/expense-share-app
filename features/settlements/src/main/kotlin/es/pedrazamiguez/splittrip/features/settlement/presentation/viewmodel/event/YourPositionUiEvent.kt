package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event

sealed class YourPositionUiEvent {
    data object Refresh : YourPositionUiEvent()
    data object ShowCashBreakdown : YourPositionUiEvent()
    data object DismissCashBreakdown : YourPositionUiEvent()
    data class ConfirmSettlement(val settlementId: String) : YourPositionUiEvent()
    data class DisputeSettlement(val settlementId: String) : YourPositionUiEvent()
    data class DisputeReasonChanged(val reason: String) : YourPositionUiEvent()
    data object DisputeSubmitted : YourPositionUiEvent()
    data object DisputeCancelled : YourPositionUiEvent()
    data class NudgeDebtor(val settlementId: String) : YourPositionUiEvent()
}
