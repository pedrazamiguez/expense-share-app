package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Event handler responsible for the Group Leave Wizard flow.
 */
interface GroupLeaveWizardEventHandler {
    val wizardState: StateFlow<LeaveWizardUiState>

    fun bind(
        scope: CoroutineScope,
        onLeaveSuccess: suspend (UiText) -> Unit,
        onError: suspend (UiText) -> Unit
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
    val isDeleting: Boolean = false
)
