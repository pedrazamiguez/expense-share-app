package es.pedrazamiguez.splittrip.features.contribution.presentation.feature

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
import es.pedrazamiguez.splittrip.features.contribution.presentation.screen.AddContributionScreen
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.AddContributionViewModel
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.event.AddContributionUiEvent
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddContributionFeature(
    addContributionViewModel: AddContributionViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    onContributionSuccess: () -> Unit = {}
) {
    val pillController = LocalTopPillController.current
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
        addContributionViewModel.actions.collectLatest { action ->
            when (action) {
                is AddContributionUiAction.ShowSuccess -> {
                    pillController.showPill(message = action.message.asString(context))
                }

                is AddContributionUiAction.ShowError -> {
                    pillController.showPill(message = action.message.asString(context))
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
