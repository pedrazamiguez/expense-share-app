package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LeaveWizardUiState(
    val showSheet: Boolean = false,
    val currentStep: LeaveWizardStep = LeaveWizardStep.BALANCE_SUMMARY,
    val activeSteps: ImmutableList<LeaveWizardStep> = persistentListOf(),
    val balanceSummary: LeaveBalanceSummaryUiModel = LeaveBalanceSummaryUiModel(),
    val settlements: ImmutableList<LeaveSettlementUiModel> = persistentListOf(),
    val cashResolution: LeaveCashResolutionUiModel = LeaveCashResolutionUiModel(),
    val subunitImpact: LeaveSubunitImpactUiModel = LeaveSubunitImpactUiModel(),
    val isLoading: Boolean = false,
    val isConfirmingSettlement: Boolean = false,
    val isLeaving: Boolean = false
)
