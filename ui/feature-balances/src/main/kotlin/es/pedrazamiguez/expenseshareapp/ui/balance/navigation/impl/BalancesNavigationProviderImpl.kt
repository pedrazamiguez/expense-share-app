package es.pedrazamiguez.expenseshareapp.ui.balance.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.ui.balance.R
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.balancesGraph

class BalancesNavigationProviderImpl(
    override val route: String = Routes.BALANCES,
    override val requiresSelectedGroup: Boolean = true,
    override val order: Int = 20,
    private val onNavigateToGroup: (String) -> Unit,
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) = NavigationBarIcon(
        icon = if (isSelected) Icons.Filled.Balance else Icons.Outlined.Balance,
        contentDescription = getLabel(),
        isSelected = isSelected
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.balances_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.balancesGraph(onNavigateToGroup = onNavigateToGroup)
    }

}
