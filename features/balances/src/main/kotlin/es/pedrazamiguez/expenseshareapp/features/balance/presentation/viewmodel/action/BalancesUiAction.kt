package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

sealed interface BalancesUiAction {
    data class ShowLoadError(val message: UiText) : BalancesUiAction
    data class ShowContributionSuccess(val message: UiText) : BalancesUiAction
    data class ShowContributionError(val message: UiText) : BalancesUiAction
}
