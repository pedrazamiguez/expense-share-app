package es.pedrazamiguez.expenseshareapp.ui.balance.navigation.impl

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import es.pedrazamiguez.expenseshareapp.core.ui.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.BALANCES_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.balancesGraph

class BalancesNavigationProviderImpl(
    private val onNavigateToGroup: (String) -> Unit,
) : NavigationProvider {

    override val route: String = BALANCES_ROUTE

    @Composable
    override fun Icon(isSelected: Boolean) {
        NavigationBarIcon(
            icon = TablerIcons.ChartBar,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Balances".placeholder

    override val order: Int = 75

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.balancesGraph(onNavigateToGroup = onNavigateToGroup)
    }

}
