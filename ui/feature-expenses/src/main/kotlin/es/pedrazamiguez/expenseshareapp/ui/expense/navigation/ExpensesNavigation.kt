package es.pedrazamiguez.expenseshareapp.ui.expense.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.feature.ExpensesFeature

fun NavGraphBuilder.expensesGraph(
) {
    composable(route = Routes.EXPENSES) {
        ExpensesFeature()
    }
}
