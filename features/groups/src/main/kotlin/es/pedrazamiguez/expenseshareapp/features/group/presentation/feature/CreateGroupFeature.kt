package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    // Collect actions as events (not state) to avoid re-triggering on config changes
    LaunchedEffect(Unit) {
        createGroupViewModel.actions.collectLatest { action ->
            when (action) {
                is CreateGroupUiAction.ShowError -> {
                    // Show snackbar with message
                    // action.message or action.messageRes
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
        })

}
