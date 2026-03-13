package es.pedrazamiguez.expenseshareapp.features.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.CreateGroupFeature
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.GroupsFeature
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.SubunitManagementFeature

fun NavGraphBuilder.groupsGraph() {
    sharedComposable(Routes.GROUPS) {
        GroupsFeature()
    }
    sharedComposable(Routes.CREATE_GROUP) {
        val navController = LocalTabNavController.current
        CreateGroupFeature(
            onCreateGroupSuccess = {
                navController.popBackStack()
            })
    }
    sharedComposable(
        route = Routes.MANAGE_SUBUNITS,
        arguments = listOf(
            navArgument("groupId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@sharedComposable
        SubunitManagementFeature(groupId = groupId)
    }
}
