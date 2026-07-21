package es.pedrazamiguez.splittrip.features.settlement.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.settlement.presentation.feature.MyPositionFeature

fun NavGraphBuilder.settlementsGraph() {
    composable(route = Routes.MY_POSITION) {
        MyPositionFeature()
    }
}
