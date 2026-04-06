package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface AddContributionUiAction {
    data class ShowSuccess(val message: UiText) : AddContributionUiAction
    data class ShowError(val message: UiText) : AddContributionUiAction
    data object NavigateBack : AddContributionUiAction
}
