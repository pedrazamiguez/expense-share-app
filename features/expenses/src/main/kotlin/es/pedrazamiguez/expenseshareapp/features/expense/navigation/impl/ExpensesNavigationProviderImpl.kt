package es.pedrazamiguez.expenseshareapp.features.expense.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.navigation.expensesGraph

class ExpensesNavigationProviderImpl(
    override val route: String = Routes.EXPENSES,
    override val requiresSelectedGroup: Boolean = true,
    override val order: Int = 50,
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean, tint: Color) = NavigationBarIcon(
        icon = if (isSelected) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
        contentDescription = getLabel(),
        isSelected = isSelected,
        tint = tint
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.expenses_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.expensesGraph()
    }

}
