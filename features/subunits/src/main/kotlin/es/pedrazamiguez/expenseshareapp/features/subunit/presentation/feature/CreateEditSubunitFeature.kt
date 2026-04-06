package es.pedrazamiguez.expenseshareapp.features.subunit.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.notification.LocalTopPillController
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.screen.CreateEditSubunitScreen
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.CreateEditSubunitViewModel
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.action.CreateEditSubunitUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateEditSubunitFeature(
    groupId: String,
    subunitId: String?,
    viewModel: CreateEditSubunitViewModel = koinViewModel<CreateEditSubunitViewModel>()
) {
    val navController = LocalTabNavController.current
    val pillController = LocalTopPillController.current
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize ViewModel with route params
    LaunchedEffect(groupId, subunitId) {
        viewModel.init(groupId, subunitId)
    }

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        viewModel.actions.collectLatest { action ->
            when (action) {
                is CreateEditSubunitUiAction.ShowSuccess -> {
                    pillController.showPill(message = action.message.asString(context))
                }

                is CreateEditSubunitUiAction.ShowError -> {
                    pillController.showPill(message = action.message.asString(context))
                }

                CreateEditSubunitUiAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    CreateEditSubunitScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
