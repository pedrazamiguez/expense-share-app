package es.pedrazamiguez.expenseshareapp.ui.expense.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.ui.expense.R
import es.pedrazamiguez.expenseshareapp.ui.expense.navigation.expensesGraph

class ExpensesNavigationProviderImpl(
    override val route: String = Routes.EXPENSES,
    override val requiresSelectedGroup: Boolean = true,
    override val order: Int = 50,
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) = NavigationBarIcon(
        icon = if (isSelected) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
        contentDescription = getLabel(),
        isSelected = isSelected
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.expenses_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.expensesGraph()
    }

}
