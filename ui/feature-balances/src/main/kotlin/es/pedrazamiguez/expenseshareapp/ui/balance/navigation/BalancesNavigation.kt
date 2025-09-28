package es.pedrazamiguez.expenseshareapp.ui.balance.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.feature.BalancesFeature

fun NavGraphBuilder.balancesGraph(
    onNavigateToGroup: (String) -> Unit,
) {
    composable(route = Routes.BALANCES) {
        BalancesFeature(
            onNavigateToGroup = onNavigateToGroup
        )
    }
}
