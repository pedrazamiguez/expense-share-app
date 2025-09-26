package es.pedrazamiguez.expenseshareapp.ui.balance.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.feature.BalancesFeature

const val BALANCES_ROUTE = "balances"

fun NavGraphBuilder.balancesGraph(
    onNavigateToGroup: (String) -> Unit,
) {
    composable(route = BALANCES_ROUTE) {
        BalancesFeature(
            onNavigateToGroup = onNavigateToGroup
        )
    }
}
