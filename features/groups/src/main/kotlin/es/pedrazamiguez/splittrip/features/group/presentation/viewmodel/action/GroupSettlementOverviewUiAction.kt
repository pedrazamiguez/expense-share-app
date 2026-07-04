package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface GroupSettlementOverviewUiAction {
    data class ShowError(val message: UiText) : GroupSettlementOverviewUiAction
    data class ShowSuccess(val message: UiText) : GroupSettlementOverviewUiAction
    data object NavigateBack : GroupSettlementOverviewUiAction
}
