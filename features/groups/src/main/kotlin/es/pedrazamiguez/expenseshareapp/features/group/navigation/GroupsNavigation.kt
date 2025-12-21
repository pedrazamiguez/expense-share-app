package es.pedrazamiguez.expenseshareapp.features.group.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.CreateGroupFeature
import es.pedrazamiguez.expenseshareapp.features.group.presentation.feature.GroupsFeature

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
}
