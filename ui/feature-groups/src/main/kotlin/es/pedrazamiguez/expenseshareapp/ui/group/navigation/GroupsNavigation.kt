package es.pedrazamiguez.expenseshareapp.ui.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.CreateGroupFeature
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.GroupsFeature

fun NavGraphBuilder.groupsGraph() {
    composable(Routes.GROUPS) {
        GroupsFeature()
    }
    composable(Routes.CREATE_GROUP) {
        CreateGroupFeature(
            onCreateGroupSuccess = {
                // Navigate back to groups after creating a group
            }
        )
    }
}
