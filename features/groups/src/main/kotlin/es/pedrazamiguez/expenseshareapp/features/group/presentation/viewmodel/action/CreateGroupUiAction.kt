package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface CreateGroupUiAction {
    data class ShowSuccess(val message: UiText) : CreateGroupUiAction
    data class ShowError(val message: UiText) : CreateGroupUiAction
}