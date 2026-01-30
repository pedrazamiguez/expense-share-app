package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.CreateGroupScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.CreateGroupViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateGroupUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateGroupFeature(
    createGroupViewModel: CreateGroupViewModel = koinViewModel<CreateGroupViewModel>(),
    onCreateGroupSuccess: () -> Unit = {}
) {

    val state by createGroupViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarController = LocalSnackbarController.current

    LaunchedEffect(Unit) {
        createGroupViewModel.actions.collectLatest { action ->
            when (action) {
                is CreateGroupUiAction.ShowSuccess -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Short
                    )
                }

                is CreateGroupUiAction.ShowError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Indefinite
                    )
                }
            }
        }
    }

    CreateGroupScreen(
        uiState = state, onEvent = { event ->
            createGroupViewModel.onEvent(
                event, onCreateGroupSuccess
            )
        })

}
