package es.pedrazamiguez.splittrip.features.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.group.presentation.feature.GroupSettlementOverviewFeature

fun NavGraphBuilder.settlementOverviewGraph() {
    sharedComposable(
        route = Routes.GROUP_SETTLEMENT_OVERVIEW,
        arguments = listOf(
            navArgument("groupId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@sharedComposable
        GroupSettlementOverviewFeature(groupId = groupId)
    }
}
