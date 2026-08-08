package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LeaveWizardUiState(
    val showSheet: Boolean = false,
    val currentStep: LeaveWizardStep = LeaveWizardStep.BALANCE_SUMMARY,
    val activeSteps: ImmutableList<LeaveWizardStep> = persistentListOf(),
    val balanceSummary: LeaveBalanceSummaryUiModel = LeaveBalanceSummaryUiModel(),
    val subunitImpact: LeaveSubunitImpactUiModel = LeaveSubunitImpactUiModel(),
    val hasUnresolvedSettlements: Boolean = false,
    val isLoading: Boolean = false,
    val isLeaving: Boolean = false
)
