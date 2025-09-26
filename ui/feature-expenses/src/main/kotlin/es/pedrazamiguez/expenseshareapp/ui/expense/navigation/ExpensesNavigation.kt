package es.pedrazamiguez.expenseshareapp.ui.expense.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.feature.ExpensesFeature

const val EXPENSES_ROUTE = "expenses"

fun NavGraphBuilder.expensesGraph(
) {
    composable(route = EXPENSES_ROUTE) {
        ExpensesFeature()
    }
}