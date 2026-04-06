package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface GroupsUiAction {
    val message: UiText
    data class ShowLoadError(override val message: UiText) : GroupsUiAction
    data class ShowDeleteSuccess(override val message: UiText) : GroupsUiAction
    data class ShowDeleteError(override val message: UiText) : GroupsUiAction
}
