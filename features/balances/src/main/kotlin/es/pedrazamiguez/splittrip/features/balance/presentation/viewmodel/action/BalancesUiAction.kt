package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface BalancesUiAction {
    val message: UiText

    data class ShowLoadError(override val message: UiText) : BalancesUiAction
    data class ShowContributionSuccess(override val message: UiText) : BalancesUiAction
    data class ShowContributionError(override val message: UiText) : BalancesUiAction
    data class ShowDeleteContributionSuccess(override val message: UiText) : BalancesUiAction
    data class ShowDeleteContributionError(override val message: UiText) : BalancesUiAction
    data class ShowDeleteWithdrawalSuccess(override val message: UiText) : BalancesUiAction
    data class ShowDeleteWithdrawalError(override val message: UiText) : BalancesUiAction
}
