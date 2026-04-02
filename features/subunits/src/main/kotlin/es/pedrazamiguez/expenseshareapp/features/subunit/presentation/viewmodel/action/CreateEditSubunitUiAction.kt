package es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface CreateEditSubunitUiAction {
    data class ShowSuccess(val message: UiText) : CreateEditSubunitUiAction
    data class ShowError(val message: UiText) : CreateEditSubunitUiAction
    data object NavigateBack : CreateEditSubunitUiAction
}
