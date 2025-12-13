package es.pedrazamiguez.expenseshareapp.features.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.CreateGroupFeature
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.GroupsFeature

fun NavGraphBuilder.groupsGraph() {
    composable(Routes.GROUPS) {
        GroupsFeature()
    }
    composable(Routes.CREATE_GROUP) {
        val navController = LocalTabNavController.current
        CreateGroupFeature(
            onCreateGroupSuccess = {
                navController.popBackStack()
            })
    }
}
