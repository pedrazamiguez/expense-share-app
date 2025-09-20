package es.pedrazamiguez.expenseshareapp.ui.expense.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.EXPENSES_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.expensesGraph

class ExpensesNavigationProviderImpl : NavigationProvider {

    override val route: String = EXPENSES_ROUTE

    override val label: String = "Expenses".placeholder

    override val icon: ImageVector = Icons.Filled.MailOutline

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.expensesGraph()
    }

}
