package es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature

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
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.AddCashWithdrawalScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.AddCashWithdrawalViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import kotlinx.coroutines.flow.collectLatest
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

    val uiState by addCashWithdrawalViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        addCashWithdrawalViewModel.actions.collectLatest { action ->
            when (action) {
                is AddCashWithdrawalUiAction.ShowError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
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

