package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface YourPositionUiAction {
    data class ShowSuccess(val message: UiText) : YourPositionUiAction
    data class ShowError(val message: UiText) : YourPositionUiAction
}
