package es.pedrazamiguez.expenseshareapp.features.balance.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.BalancesFeature

fun NavGraphBuilder.balancesGraph(
    onNavigateToGroup: (String) -> Unit,
) {
    composable(route = Routes.BALANCES) {
        BalancesFeature(
            onNavigateToGroup = onNavigateToGroup
        )
    }
}
