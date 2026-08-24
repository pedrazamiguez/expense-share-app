package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event

import es.pedrazamiguez.splittrip.features.settings.presentation.model.BillingInterval
import es.pedrazamiguez.splittrip.features.settings.presentation.model.SubscriptionTier

sealed interface SubscriptionsUiEvent {
    data object LoadSubscriptions : SubscriptionsUiEvent
    data class SelectBillingInterval(val interval: BillingInterval) : SubscriptionsUiEvent
    data class UpgradePlan(val tier: SubscriptionTier) : SubscriptionsUiEvent
    data object RestorePurchases : SubscriptionsUiEvent
}
