package es.pedrazamiguez.expenseshareapp.features.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.feature.ProfileFeature

fun NavGraphBuilder.profileGraph(
) {
    composable(route = Routes.PROFILE) {
        ProfileFeature()
    }
}
