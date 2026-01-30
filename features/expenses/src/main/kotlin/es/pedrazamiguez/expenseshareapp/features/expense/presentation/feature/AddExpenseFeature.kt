package es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.AddExpenseScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.AddExpenseViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddExpenseFeature(
    addExpenseViewModel: AddExpenseViewModel = koinViewModel<AddExpenseViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    onAddExpenseSuccess: () -> Unit = {}
) {

    val state by addExpenseViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId = sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    AddExpenseScreen(
        groupId = selectedGroupId.value,
        uiState = state,
        onEvent = { event ->
            addExpenseViewModel.onEvent(
                event,
                onAddExpenseSuccess
            )
        })
}
