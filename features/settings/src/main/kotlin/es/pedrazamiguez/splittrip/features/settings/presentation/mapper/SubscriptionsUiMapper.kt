package es.pedrazamiguez.splittrip.features.settings.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.enums.SubscriptionTier
import es.pedrazamiguez.splittrip.features.settings.presentation.model.BillingInterval
import es.pedrazamiguez.splittrip.features.settings.presentation.model.SubscriptionPlanUiModel
import kotlinx.collections.immutable.ImmutableList

interface SubscriptionsUiMapper {
    fun mapPlans(
        currentTier: SubscriptionTier,
        selectedInterval: BillingInterval
    ): ImmutableList<SubscriptionPlanUiModel>

    fun formatSavingsBadge(): UiText
    fun formatUpgradeSuccessMessage(tier: SubscriptionTier): UiText
    fun formatRestorePurchasesSuccessMessage(): UiText
}
