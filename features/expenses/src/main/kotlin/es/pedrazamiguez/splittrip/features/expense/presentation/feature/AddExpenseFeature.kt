package es.pedrazamiguez.splittrip.features.expense.presentation.feature

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.common.presentation.asString
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.presentation.notification.LocalTopPillController
import es.pedrazamiguez.splittrip.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.splittrip.features.expense.presentation.screen.AddExpenseScreen
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.AddExpenseViewModel
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
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
    val pillController = LocalTopPillController.current
    val context = LocalContext.current
    val navController = LocalTabNavController.current

    val state by addExpenseViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId = sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    // Intercept system back — delegate to wizard navigation
    BackHandler {
        addExpenseViewModel.onEvent(AddExpenseUiEvent.PreviousStep)
    }

    // Collect side-effect actions and route them to the global pill notification.
    LaunchedEffect(Unit) {
        addExpenseViewModel.actions.collectLatest { action ->
            when (action) {
                is AddExpenseUiAction.ShowError -> {
                    pillController.showPill(message = action.message.asString(context))
                }

                is AddExpenseUiAction.ShowCashConflictError -> {
                    pillController.showPill(message = action.message.asString(context))
                    addExpenseViewModel.refreshCashPreview()
                }

                AddExpenseUiAction.NavigateBack -> {
                    navController.popBackStack()
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
