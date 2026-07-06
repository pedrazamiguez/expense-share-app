package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action.GroupDetailUiAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Event handler responsible for the Group Leave Wizard flow.
 */
interface GroupLeaveWizardEventHandler {
    fun bind(
        localUiStateFlow: MutableStateFlow<GroupDetailViewModelLocalState>,
        actionsChannel: Channel<GroupDetailUiAction>,
        scope: CoroutineScope
    )

    fun handleLeaveClicked(groupId: String)
    fun handleWizardNext(groupId: String)
    fun handleWizardBack()
    fun handleWizardCancelled()
    fun handleConfirmSettlement(groupId: String, settlementId: String)
    fun handleLeave(groupId: String)
}

/**
 * Internal state maintained by [es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.GroupDetailViewModel].
 */
data class GroupDetailViewModelLocalState(
    val showArchiveConfirmation: Boolean = false,
    val isArchiving: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false,
    val isLeaving: Boolean = false,
    val leaveWizardState: es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState =
        es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState()
)
