package es.pedrazamiguez.splittrip.features.settlement.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.settlement.presentation.feature.MyPositionFeature

fun NavGraphBuilder.settlementsGraph() {
    sharedComposable(route = Routes.MY_POSITION) {
        MyPositionFeature()
    }
}
