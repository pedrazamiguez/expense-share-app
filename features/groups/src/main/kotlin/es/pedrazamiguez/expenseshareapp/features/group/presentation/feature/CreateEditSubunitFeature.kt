package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.CreateEditSubunitScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.CreateEditSubunitViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateEditSubunitUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateEditSubunitFeature(
    groupId: String,
    subunitId: String?,
    viewModel: CreateEditSubunitViewModel = koinViewModel<CreateEditSubunitViewModel>()
) {
    val navController = LocalTabNavController.current
    val snackbarController = LocalSnackbarController.current
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
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Short
                    )
                }

                is CreateEditSubunitUiAction.ShowError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
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

