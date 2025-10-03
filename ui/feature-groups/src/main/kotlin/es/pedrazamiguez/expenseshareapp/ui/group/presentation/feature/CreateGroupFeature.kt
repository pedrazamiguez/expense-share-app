package es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.model.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.CreateGroupScreen
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.viewmodel.CreateGroupViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateGroupFeature(
    createGroupViewModel: CreateGroupViewModel = koinViewModel<CreateGroupViewModel>(),
    onCreateGroupSuccess: () -> Unit = {}
) {

    val state by createGroupViewModel.uiState.collectAsState()
    createGroupViewModel.actions.collectAsStateWithLifecycle(CreateGroupUiAction.None)

    CreateGroupScreen(
        uiState = state,
        onEvent = { event ->
            createGroupViewModel.onEvent(
                event,
                onCreateGroupSuccess
            )
        })

}
