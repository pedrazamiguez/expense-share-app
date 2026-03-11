package es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface ProfileUiAction {
    data class ShowError(val message: UiText) : ProfileUiAction
}

