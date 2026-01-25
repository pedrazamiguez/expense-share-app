package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.CreateGroupScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.CreateGroupViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateGroupUiAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateGroupFeature(
    createGroupViewModel: CreateGroupViewModel = koinViewModel<CreateGroupViewModel>(),
    onCreateGroupSuccess: () -> Unit = {}
) {

    val state by createGroupViewModel.uiState.collectAsStateWithLifecycle()
    val actions = createGroupViewModel.actions.collectAsStateWithLifecycle(CreateGroupUiAction.None)

    LaunchedEffect(actions.value) {
        when (val action = actions.value) {
            CreateGroupUiAction.None -> {
                // Noop
            }

            is CreateGroupUiAction.ShowError -> {
                // Show snackbar with message
                // action.message

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
