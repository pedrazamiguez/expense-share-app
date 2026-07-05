package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event

sealed interface GroupSettlementOverviewUiEvent {
    data class ConfirmSettlement(val settlementId: String) : GroupSettlementOverviewUiEvent
    data class DisputeSettlement(val settlementId: String) : GroupSettlementOverviewUiEvent
    data class DisputeReasonChanged(val reason: String) : GroupSettlementOverviewUiEvent
    data object DisputeSubmitted : GroupSettlementOverviewUiEvent
    data object DisputeCancelled : GroupSettlementOverviewUiEvent
    data object CloseTripClicked : GroupSettlementOverviewUiEvent
}
