package es.pedrazamiguez.expenseshareapp.ui.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.CreateGroupFeature
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.GroupsFeature

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
