package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.screen.AddExpenseScreen
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.viewmodel.AddExpenseViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddExpenseFeature(
    addExpenseViewModel: AddExpenseViewModel = koinViewModel<AddExpenseViewModel>(),
    onAddExpenseSuccess: () -> Unit = {}
) {

    val state by addExpenseViewModel.uiState.collectAsState()

    AddExpenseScreen(
        uiState = state,
        onEvent = { event ->
            addExpenseViewModel.onEvent(
                event,
                onAddExpenseSuccess
            )
        })
}