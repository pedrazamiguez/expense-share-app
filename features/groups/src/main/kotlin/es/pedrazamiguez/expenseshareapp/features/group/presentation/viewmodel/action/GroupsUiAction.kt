package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface GroupsUiAction {
    data class ShowDeleteSuccess(val message: UiText) : GroupsUiAction
    data class ShowDeleteError(val message: UiText) : GroupsUiAction
}
