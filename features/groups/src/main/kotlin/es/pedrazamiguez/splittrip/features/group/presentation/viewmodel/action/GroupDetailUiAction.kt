package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface GroupDetailUiAction {
    val message: UiText?
    data class ShowError(override val message: UiText) : GroupDetailUiAction
    data class ArchiveSuccess(override val message: UiText) : GroupDetailUiAction
    data class DeleteSuccess(override val message: UiText) : GroupDetailUiAction
    data class LeaveSuccess(override val message: UiText) : GroupDetailUiAction
    data class NavigateToSettlementOverview(val groupId: String) : GroupDetailUiAction {
        override val message: UiText? = null
    }
    data object NavigateToYourBalance : GroupDetailUiAction {
        override val message: UiText? = null
    }
}
