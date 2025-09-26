package es.pedrazamiguez.expenseshareapp.ui.expense.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
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
            icon = if (isSelected) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Expenses".placeholder

    override val order: Int = 50

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.expensesGraph()
    }

}
