package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface GroupDetailUiAction {
    val message: UiText
    data class ShowError(override val message: UiText) : GroupDetailUiAction
    data class DeleteSuccess(override val message: UiText) : GroupDetailUiAction
    data class LeaveSuccess(override val message: UiText) : GroupDetailUiAction
}
