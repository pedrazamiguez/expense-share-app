package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.notification.LocalTopPillController
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
    val pillController = LocalTopPillController.current
    val navController = LocalTabNavController.current

    LaunchedEffect(Unit) {
        createGroupViewModel.actions.collectLatest { action ->
            when (action) {
                is CreateGroupUiAction.ShowSuccess -> {
                    pillController.showPill(message = action.message.asString(context))
                }

                is CreateGroupUiAction.ShowError -> {
                    pillController.showPill(message = action.message.asString(context))
                }

                CreateGroupUiAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    CreateGroupScreen(
        uiState = state,
        onEvent = { event ->
            createGroupViewModel.onEvent(
                event,
                onCreateGroupSuccess
            )
        }
    )
}
