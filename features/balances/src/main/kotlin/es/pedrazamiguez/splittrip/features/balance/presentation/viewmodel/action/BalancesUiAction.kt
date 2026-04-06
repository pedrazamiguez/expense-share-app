package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface BalancesUiAction {
    data class ShowLoadError(val message: UiText) : BalancesUiAction
    data class ShowContributionSuccess(val message: UiText) : BalancesUiAction
    data class ShowContributionError(val message: UiText) : BalancesUiAction
}
