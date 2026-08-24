package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.action

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

sealed interface SubscriptionsUiAction {
    data class ShowTopPill(val message: UiText) : SubscriptionsUiAction
    data object NavigateBack : SubscriptionsUiAction
}
