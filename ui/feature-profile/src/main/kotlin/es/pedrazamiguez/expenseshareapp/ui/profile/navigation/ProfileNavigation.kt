package es.pedrazamiguez.expenseshareapp.ui.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.profile.presentation.feature.ProfileFeature

const val PROFILE_ROUTE = "profile"

fun NavGraphBuilder.profileGraph(
) {
    composable(route = PROFILE_ROUTE) {
        ProfileFeature()
    }
}
