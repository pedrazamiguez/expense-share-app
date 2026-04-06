package es.pedrazamiguez.splittrip.features.withdrawal.presentation.feature

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
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.screen.AddCashWithdrawalScreen
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.AddCashWithdrawalViewModel
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.event.AddCashWithdrawalUiEvent
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
    val pillController = LocalTopPillController.current
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
        addCashWithdrawalViewModel.actions.collectLatest { action ->
            when (action) {
                is AddCashWithdrawalUiAction.ShowError -> {
                    pillController.showPill(message = action.message.asString(context))
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
