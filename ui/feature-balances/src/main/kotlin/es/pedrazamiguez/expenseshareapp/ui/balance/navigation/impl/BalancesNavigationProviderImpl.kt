package es.pedrazamiguez.expenseshareapp.ui.balance.navigation.impl

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.BALANCES_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.balance.navigation.balancesGraph

class BalancesNavigationProviderImpl(
    private val onNavigateToGroup: (String) -> Unit,
) : NavigationProvider {

    override val route: String = BALANCES_ROUTE

    override val label: String = "Balances".placeholder

    @Composable
    override fun Icon(isSelected: Boolean) {
        val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1f)

        Icon(
            imageVector = TablerIcons.AlertTriangle,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size((24.dp * scale))
        )
    }

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.balancesGraph(onNavigateToGroup = onNavigateToGroup)
    }

}
