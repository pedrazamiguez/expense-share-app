package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface SubunitManagementUiAction {
    data class ShowSuccess(val message: UiText) : SubunitManagementUiAction
    data class ShowError(val message: UiText) : SubunitManagementUiAction
}

