package es.pedrazamiguez.expenseshareapp.ui.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.profile.presentation.feature.ProfileFeature

fun NavGraphBuilder.profileGraph(
) {
    composable(route = Routes.PROFILE) {
        ProfileFeature()
    }
}
