package es.pedrazamiguez.expenseshareapp.features.expense.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature.AddExpenseFeature
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature.ExpensesFeature

fun NavGraphBuilder.expensesGraph() {
    sharedComposable(route = Routes.EXPENSES) {
        ExpensesFeature()
    }

    sharedComposable(route = Routes.ADD_EXPENSE) {
        val navController = LocalTabNavController.current
        AddExpenseFeature(
            onAddExpenseSuccess = {
                navController.popBackStack()
            })
    }
}
