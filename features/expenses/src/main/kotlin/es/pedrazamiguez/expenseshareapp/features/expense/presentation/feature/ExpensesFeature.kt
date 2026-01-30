package es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.ExpensesScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.ExpensesViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun ExpensesFeature(
    expensesViewModel: ExpensesViewModel = koinViewModel<ExpensesViewModel>()
) {
    val navController = LocalTabNavController.current

    val uiState by expensesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        expensesViewModel.onEvent(ExpensesUiEvent.LoadExpenses)
    }

    ExpensesScreen(
        uiState = uiState,
        onExpenseClicked = { expenseId ->
            Timber.d("Expense clicked: $expenseId")
        },
        onAddExpenseClick = {
            navController.navigate(Routes.ADD_EXPENSE)
        },
        onScrollPositionChanged = { index, offset ->
            expensesViewModel.onEvent(ExpensesUiEvent.ScrollPositionChanged(index, offset))
        }
    )
}
