package es.pedrazamiguez.expenseshareapp.ui.balance.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.balancesGraph

class BalancesNavigationProviderImpl(
    override val route: String = Routes.BALANCES,
    override val requiresSelectedGroup: Boolean = true,
    private val onNavigateToGroup: (String) -> Unit,
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) {
        NavigationBarIcon(
            icon = if (isSelected) Icons.Filled.Balance else Icons.Outlined.Balance,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Balances".hardcoded

    override val order: Int = 20

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.balancesGraph(onNavigateToGroup = onNavigateToGroup)
    }

}
