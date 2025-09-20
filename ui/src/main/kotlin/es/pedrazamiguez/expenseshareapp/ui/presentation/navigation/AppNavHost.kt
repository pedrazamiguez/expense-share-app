package es.pedrazamiguez.expenseshareapp.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.BALANCES_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.balanceGraph

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController, startDestination = BALANCES_ROUTE, modifier = modifier
    ) {
        // ✅ Balances feature
        balanceGraph(
            onNavigateToGroup = { groupId ->
                navController.navigate("groups/$groupId")
            })

        // 🚧 TODO: add other feature graphs when ready
        // authGraph(...)
        // expensesGraph(...)
        // settingsGraph(...)
    }
}
