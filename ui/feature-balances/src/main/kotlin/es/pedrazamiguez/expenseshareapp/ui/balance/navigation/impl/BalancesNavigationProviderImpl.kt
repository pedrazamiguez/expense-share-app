package es.pedrazamiguez.expenseshareapp.ui.balance.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.BALANCES_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.balancesGraph

class BalancesNavigationProviderImpl(
    private val onNavigateToGroup: (String) -> Unit,
) : NavigationProvider {

    override val route: String = BALANCES_ROUTE

    override val label: String = "Balances".placeholder

    override val icon: ImageVector = Icons.Filled.ShoppingCart

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.balancesGraph(onNavigateToGroup = onNavigateToGroup)
    }

}
