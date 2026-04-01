package es.pedrazamiguez.expenseshareapp.features.subunit.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.screen.SubunitManagementScreen
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.SubunitManagementViewModel
import es.pedrazamiguez.expenseshareapp.features.subunit.presentation.viewmodel.action.SubunitManagementUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubunitManagementFeature(
    groupId: String,
    viewModel: SubunitManagementViewModel = koinViewModel<SubunitManagementViewModel>()
) {
    val navController = LocalTabNavController.current
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

                is SubunitManagementUiAction.NavigateToCreateSubunit -> {
                    navController.navigate(Routes.createEditSubunitRoute(action.groupId))
                }

                is SubunitManagementUiAction.NavigateToEditSubunit -> {
                    navController.navigate(
                        Routes.createEditSubunitRoute(action.groupId, action.subunitId)
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
