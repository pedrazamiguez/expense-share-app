package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event

import es.pedrazamiguez.splittrip.features.settlement.presentation.model.archive.ArchiveWizardStep

sealed interface GroupSettlementOverviewUiEvent {
    data class ConfirmSettlement(val settlementId: String) : GroupSettlementOverviewUiEvent
    data class DisputeSettlement(val settlementId: String) : GroupSettlementOverviewUiEvent
    data class DisputeReasonChanged(val reason: String) : GroupSettlementOverviewUiEvent
    data object DisputeSubmitted : GroupSettlementOverviewUiEvent
    data object DisputeCancelled : GroupSettlementOverviewUiEvent
    data object CloseTripClicked : GroupSettlementOverviewUiEvent
    data object WizardNextClicked : GroupSettlementOverviewUiEvent
    data object WizardBackClicked : GroupSettlementOverviewUiEvent
    data object WizardCancelled : GroupSettlementOverviewUiEvent
    data class WizardJumpToStep(val step: ArchiveWizardStep) : GroupSettlementOverviewUiEvent
    data object NavigateToYourBalanceClicked : GroupSettlementOverviewUiEvent
}
