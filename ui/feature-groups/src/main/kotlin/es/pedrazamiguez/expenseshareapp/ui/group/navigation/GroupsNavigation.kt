package es.pedrazamiguez.expenseshareapp.ui.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.GroupsFeature

fun NavGraphBuilder.groupsGraph(
) {
    composable(route = Routes.GROUPS) {
        GroupsFeature()
    }
}
