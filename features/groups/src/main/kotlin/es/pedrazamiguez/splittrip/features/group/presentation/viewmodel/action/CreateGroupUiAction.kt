package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface CreateGroupUiAction {
    data class ShowSuccess(val message: UiText) : CreateGroupUiAction
    data class ShowError(val message: UiText) : CreateGroupUiAction
    data object NavigateBack : CreateGroupUiAction
}
