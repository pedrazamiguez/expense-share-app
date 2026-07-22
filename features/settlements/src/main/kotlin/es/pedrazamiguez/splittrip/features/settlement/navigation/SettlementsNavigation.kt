package es.pedrazamiguez.splittrip.features.settlement.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.settlement.presentation.feature.YourPositionFeature

fun NavGraphBuilder.settlementsGraph() {
    sharedComposable(route = Routes.YOUR_POSITION) {
        YourPositionFeature()
    }
}
