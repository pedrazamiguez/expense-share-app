package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.SubunitManagementScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.SubunitManagementViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.SubunitManagementUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubunitManagementFeature(
    groupId: String,
    viewModel: SubunitManagementViewModel = koinViewModel<SubunitManagementViewModel>()
) {
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Set the groupId for the ViewModel
    LaunchedEffect(groupId) {
        viewModel.setGroupId(groupId)
    }

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        viewModel.actions.collectLatest { action ->
            when (action) {
                is SubunitManagementUiAction.ShowSuccess -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Short
                    )
                }

                is SubunitManagementUiAction.ShowError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    SubunitManagementScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

