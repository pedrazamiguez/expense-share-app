package es.pedrazamiguez.splittrip.features.expense.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.AddExpenseFeature
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.ExpenseDetailFeature
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.ExpensesFeature
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.ExpensesFilterFeature
import es.pedrazamiguez.splittrip.features.expense.presentation.feature.ReceiptViewerFeature

@Suppress("LongMethod")
fun NavGraphBuilder.expensesGraph() {
    sharedComposable(route = Routes.EXPENSES) {
        ExpensesFeature()
    }

    sharedComposable(route = Routes.EXPENSES_FILTER) {
        val navController = LocalTabNavController.current
        val initialCriteria = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<ExpenseFilterCriteria>("initialFilterCriteria") ?: ExpenseFilterCriteria()
        ExpensesFilterFeature(
            initialCriteria = initialCriteria,
            onApplyFilters = { appliedCriteria ->
                navController.previousBackStackEntry?.savedStateHandle?.set("appliedFilterCriteria", appliedCriteria)
                navController.popBackStack()
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    sharedComposable(route = Routes.ADD_EXPENSE) {
        val navController = LocalTabNavController.current
        AddExpenseFeature(
            onAddExpenseSuccess = {
                navController.previousBackStackEntry?.savedStateHandle?.set("expenseAdded", true)
                navController.popBackStack()
            }
        )
    }

    sharedComposable(
        route = Routes.EDIT_EXPENSE,
        arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
    ) { backStackEntry ->
        val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@sharedComposable
        val navController = LocalTabNavController.current
        AddExpenseFeature(
            expenseId = expenseId,
            onAddExpenseSuccess = {
                navController.popBackStack()
            }
        )
    }

    sharedComposable(
        route = Routes.EXPENSE_DETAIL,
        arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
    ) { backStackEntry ->
        val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@sharedComposable
        ExpenseDetailFeature(expenseId = expenseId)
    }

    sharedComposable(
        route = Routes.RECEIPT_VIEWER,
        arguments = listOf(
            navArgument("receiptUri") { type = NavType.StringType },
            navArgument("mimeType") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val receiptUri = backStackEntry.arguments?.getString("receiptUri") ?: return@sharedComposable
        val mimeType = backStackEntry.arguments?.getString("mimeType")
        ReceiptViewerFeature(receiptUri = receiptUri, mimeType = mimeType)
    }
}
