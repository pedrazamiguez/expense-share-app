package es.pedrazamiguez.splittrip.features.settings.presentation.model

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import kotlinx.collections.immutable.ImmutableList

data class SubscriptionPlanUiModel(
    val tier: SubscriptionTier,
    val title: UiText,
    val description: UiText,
    val price: UiText,
    val period: UiText,
    val badge: UiText? = null,
    val features: ImmutableList<SubscriptionFeatureUiModel>,
    val isCurrentPlan: Boolean,
    val ctaButtonText: UiText,
    val isCtaButtonEnabled: Boolean,
    val isHighlightedCard: Boolean
)
