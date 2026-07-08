package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState

data class GroupDetailUiState(
    val group: GroupUiModel? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val subunitsCount: Int = 0,
    val isOnlyGroup: Boolean = false,
    val showArchiveConfirmation: Boolean = false,
    val isUserAdmin: Boolean = false,
    val isArchiving: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false,
    val isLeaving: Boolean = false,
    val leaveWizardState: LeaveWizardUiState = LeaveWizardUiState()
)
