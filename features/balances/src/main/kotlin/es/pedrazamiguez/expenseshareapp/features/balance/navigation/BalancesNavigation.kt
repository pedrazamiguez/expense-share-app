package es.pedrazamiguez.expenseshareapp.features.balance.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.BalancesFeature

fun NavGraphBuilder.balancesGraph() {
    sharedComposable(route = Routes.BALANCES) {
        BalancesFeature()
    }
}
