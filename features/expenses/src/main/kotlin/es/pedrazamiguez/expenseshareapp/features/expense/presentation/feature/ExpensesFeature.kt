package es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.ExpensesScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.ListGroupExpensesViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun ExpensesFeature(
    listGroupExpensesViewModel: ListGroupExpensesViewModel = koinViewModel<ListGroupExpensesViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel<SharedViewModel>()
) {

    val expenses by listGroupExpensesViewModel.expenses.collectAsState()
    val loading by listGroupExpensesViewModel.loading.collectAsState()
    val error by listGroupExpensesViewModel.error.collectAsState()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsState()

    LaunchedEffect(selectedGroupId) {
        selectedGroupId?.let { groupId ->
            listGroupExpensesViewModel.fetchExpensesFlow(groupId)
        }
    }

    ExpensesScreen(
        expenses = expenses,
        loading = loading,
        errorMessage = error,
        onExpenseClicked = { expenseId ->
            Timber.d("Expense clicked: $expenseId")
        })

}
