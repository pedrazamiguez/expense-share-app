package es.pedrazamiguez.splittrip.features.expense.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.AddExpenseFeature
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.ExpensesFeature

fun NavGraphBuilder.expensesGraph() {
    sharedComposable(route = Routes.EXPENSES) {
        ExpensesFeature()
    }

    sharedComposable(route = Routes.ADD_EXPENSE) {
        val navController = LocalTabNavController.current
        AddExpenseFeature(
            onAddExpenseSuccess = {
                navController.popBackStack()
            }
        )
    }
}
