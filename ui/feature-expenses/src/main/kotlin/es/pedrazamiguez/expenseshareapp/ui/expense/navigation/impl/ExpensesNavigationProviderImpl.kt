package es.pedrazamiguez.expenseshareapp.ui.expense.navigation.impl

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.Receipt
import es.pedrazamiguez.expenseshareapp.core.ui.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.EXPENSES_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.expensesGraph

class ExpensesNavigationProviderImpl : NavigationProvider {

    override val route: String = EXPENSES_ROUTE

    @Composable
    override fun Icon(isSelected: Boolean) {
        NavigationBarIcon(
            icon = TablerIcons.Receipt,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Expenses".placeholder

    override val order: Int = 20

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.expensesGraph()
    }

}
