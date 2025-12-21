package es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.ExpensesScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.ListGroupExpensesViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun ExpensesFeature(
    listGroupExpensesViewModel: ListGroupExpensesViewModel = koinViewModel<ListGroupExpensesViewModel>()
) {
    val navController = LocalTabNavController.current

    val expenses by listGroupExpensesViewModel.expenses.collectAsState()
    val loading by listGroupExpensesViewModel.loading.collectAsState()
    val error by listGroupExpensesViewModel.error.collectAsState()

    ExpensesScreen(
        expenses = expenses,
        loading = loading,
        errorMessage = error,
        onExpenseClicked = { expenseId ->
            Timber.d("Expense clicked: $expenseId")
        },
        onAddExpenseClick = {
            navController.navigate(Routes.ADD_EXPENSE)
        }
    )
}
