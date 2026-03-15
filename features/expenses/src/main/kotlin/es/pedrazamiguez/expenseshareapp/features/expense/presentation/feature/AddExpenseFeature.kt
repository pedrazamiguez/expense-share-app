package es.pedrazamiguez.expenseshareapp.features.expense.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.AddExpenseScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.AddExpenseViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddExpenseFeature(
    addExpenseViewModel: AddExpenseViewModel = koinViewModel<AddExpenseViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    onAddExpenseSuccess: () -> Unit = {}
) {
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current

    val state by addExpenseViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId = sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    // Collect side-effect actions and route them to the global snackbar.
    LaunchedEffect(Unit) {
        addExpenseViewModel.actions.collectLatest { action ->
            when (action) {
                is AddExpenseUiAction.ShowError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
                }

                AddExpenseUiAction.None -> Unit
            }
        }
    }

    AddExpenseScreen(
        groupId = selectedGroupId.value,
        uiState = state,
        onEvent = { event ->
            addExpenseViewModel.onEvent(
                event,
                onAddExpenseSuccess
            )
        }
    )
}
