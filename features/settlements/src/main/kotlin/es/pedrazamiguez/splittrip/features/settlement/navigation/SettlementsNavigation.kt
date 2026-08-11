package es.pedrazamiguez.splittrip.features.settlement.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.settlement.presentation.feature.GroupSettlementOverviewFeature
import es.pedrazamiguez.splittrip.features.settlement.presentation.feature.YourBalanceFeature

fun NavGraphBuilder.settlementsGraph() {
    sharedComposable(route = Routes.YOUR_BALANCE) {
        YourBalanceFeature()
    }
    sharedComposable(route = Routes.GROUP_SETTLEMENT_OVERVIEW) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@sharedComposable
        GroupSettlementOverviewFeature(groupId = groupId)
    }
}
