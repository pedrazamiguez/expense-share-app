package es.pedrazamiguez.splittrip.features.settings.presentation.model

import es.pedrazamiguez.splittrip.core.common.presentation.UiText

data class SubscriptionFeatureUiModel(
    val label: UiText,
    val isIncluded: Boolean,
    val isHighlighted: Boolean = false
)
