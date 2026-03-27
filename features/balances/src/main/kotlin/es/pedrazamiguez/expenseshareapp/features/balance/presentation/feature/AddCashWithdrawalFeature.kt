package es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.AddCashWithdrawalScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.AddCashWithdrawalViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddCashWithdrawalFeature(
    addCashWithdrawalViewModel: AddCashWithdrawalViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    onWithdrawalSuccess: () -> Unit = {}
) {
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current
    val navController = LocalTabNavController.current

    val uiState by addCashWithdrawalViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    // Intercept system back — delegate to wizard navigation
    BackHandler {
        addCashWithdrawalViewModel.onEvent(AddCashWithdrawalUiEvent.PreviousStep)
    }

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        addCashWithdrawalViewModel.actions.collect { action ->
            when (action) {
                is AddCashWithdrawalUiAction.ShowError -> {
                    launch {
                        snackbarController.showSnackbar(
                            message = action.message.asString(context),
                            duration = SnackbarDuration.Long
                        )
                    }
                }

                AddCashWithdrawalUiAction.NavigateBack -> {
                    navController.popBackStack()
                }

                AddCashWithdrawalUiAction.None -> { /* no-op */ }
            }
        }
    }

    AddCashWithdrawalScreen(
        groupId = selectedGroupId,
        uiState = uiState,
        onEvent = { event ->
            addCashWithdrawalViewModel.onEvent(event, onWithdrawalSuccess)
        }
    )
}
