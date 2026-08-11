package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event

import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardStep

sealed interface GroupDetailUiEvent {
    data object ArchiveClicked : GroupDetailUiEvent
    data object DeleteClicked : GroupDetailUiEvent
    data object DeleteConfirmed : GroupDetailUiEvent
    data object DeleteCancelled : GroupDetailUiEvent
    data object LeaveClicked : GroupDetailUiEvent
    data object LeaveConfirmed : GroupDetailUiEvent
    data object LeaveCancelled : GroupDetailUiEvent
    data object WizardNextClicked : GroupDetailUiEvent
    data object WizardBackClicked : GroupDetailUiEvent
    data object WizardCancelled : GroupDetailUiEvent
    data object NavigateToYourBalanceClicked : GroupDetailUiEvent
    data class WizardJumpToStepClicked(val step: LeaveWizardStep) : GroupDetailUiEvent
}
