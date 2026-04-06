package es.pedrazamiguez.splittrip.features.subunit.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.subunit.presentation.feature.CreateEditSubunitFeature
import es.pedrazamiguez.splittrip.features.subunit.presentation.feature.SubunitManagementFeature

fun NavGraphBuilder.subunitsGraph() {
    sharedComposable(
        route = Routes.MANAGE_SUBUNITS,
        arguments = listOf(
            navArgument("groupId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@sharedComposable
        SubunitManagementFeature(groupId = groupId)
    }
    sharedComposable(
        route = Routes.CREATE_EDIT_SUBUNIT,
        arguments = listOf(
            navArgument("groupId") { type = NavType.StringType },
            navArgument("subunitId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@sharedComposable
        val subunitId = backStackEntry.arguments?.getString("subunitId")
        CreateEditSubunitFeature(groupId = groupId, subunitId = subunitId)
    }
}
