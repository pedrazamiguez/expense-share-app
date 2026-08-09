package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementRowUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.archive.ArchiveWizardStep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class GroupSettlementOverviewUiState(
    val pendingSettlements: ImmutableList<SettlementRowUiModel> = persistentListOf(),
    val disputedSettlements: ImmutableList<SettlementRowUiModel> = persistentListOf(),
    val resolvedSettlements: ImmutableList<SettlementRowUiModel> = persistentListOf(),
    val areAllSettlementsResolved: Boolean = false,
    val isLoading: Boolean = true,
    val isArchiving: Boolean = false,
    val activeDisputeSettlementId: String? = null,
    val disputeReasonInput: String = "",
    val hasError: Boolean = false,
    val currentStep: ArchiveWizardStep = ArchiveWizardStep.SETTLEMENT_SUMMARY,
    val activeSteps: ImmutableList<ArchiveWizardStep> = persistentListOf(),
    val isUserCreator: Boolean = false,
    val actionRequiredCount: Int = 0,
    val waitingOnOthersCount: Int = 0,
    val disputedCount: Int = 0,
    val groupName: String = ""
)
