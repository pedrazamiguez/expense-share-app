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
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.AddContributionScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.AddContributionViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddContributionFeature(
    addContributionViewModel: AddContributionViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    onContributionSuccess: () -> Unit = {}
) {
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current
    val navController = LocalTabNavController.current

    val uiState by addContributionViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    // Intercept system back — delegate to wizard navigation
    BackHandler {
        addContributionViewModel.onEvent(AddContributionUiEvent.PreviousStep)
    }

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        addContributionViewModel.actions.collect { action ->
            when (action) {
                is AddContributionUiAction.ShowSuccess -> {
                    launch {
                        snackbarController.showSnackbar(
                            message = action.message.asString(context),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is AddContributionUiAction.ShowError -> {
                    launch {
                        snackbarController.showSnackbar(
                            message = action.message.asString(context),
                            duration = SnackbarDuration.Long
                        )
                    }
                }

                AddContributionUiAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    AddContributionScreen(
        groupId = selectedGroupId,
        uiState = uiState,
        onEvent = { event ->
            addContributionViewModel.onEvent(event, onContributionSuccess)
        }
    )
}
