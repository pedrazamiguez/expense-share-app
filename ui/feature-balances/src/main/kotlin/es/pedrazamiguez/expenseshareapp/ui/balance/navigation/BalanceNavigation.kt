package es.pedrazamiguez.expenseshareapp.ui.balance.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.ui.BalanceRoute

const val BALANCES_ROUTE = "balances"

fun NavGraphBuilder.balanceGraph(
    onNavigateToGroup: (String) -> Unit,
) {
    composable(route = BALANCES_ROUTE) {
        BalanceRoute(
            onNavigateToGroup = onNavigateToGroup
        )
    }
}
