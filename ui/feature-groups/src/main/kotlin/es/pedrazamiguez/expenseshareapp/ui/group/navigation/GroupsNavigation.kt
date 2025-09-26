package es.pedrazamiguez.expenseshareapp.ui.group.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.GroupsFeature

const val GROUPS_ROUTE = "groups"

fun NavGraphBuilder.groupsGraph(
) {
    composable(route = GROUPS_ROUTE) {
        GroupsFeature()
    }
}
