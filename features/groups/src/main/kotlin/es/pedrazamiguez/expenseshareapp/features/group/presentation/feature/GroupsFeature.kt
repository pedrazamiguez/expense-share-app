package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.GroupsScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.GroupsViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.GroupsUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.GroupsUiEvent
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupsFeature(
    groupsViewModel: GroupsViewModel = koinViewModel<GroupsViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    )
) {
    val navController = LocalTabNavController.current
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current

    val uiState by groupsViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        groupsViewModel.onEvent(GroupsUiEvent.LoadGroups)
    }

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        groupsViewModel.actions.collectLatest { action ->
            when (action) {
                is GroupsUiAction.ShowDeleteSuccess -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Short
                    )
                }

                is GroupsUiAction.ShowDeleteError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    GroupsScreen(
        uiState = uiState,
        selectedGroupId = selectedGroupId,
        onGroupClicked = { groupId, groupName ->
            if (groupId != selectedGroupId) {
                sharedViewModel.selectGroup(groupId, groupName)
            } else {
                sharedViewModel.selectGroup(null, null)
            }
        },
        onCreateGroupClick = {
            navController.navigate(Routes.CREATE_GROUP)
        },
        onScrollPositionChanged = { index, offset ->
            groupsViewModel.onEvent(
                GroupsUiEvent.ScrollPositionChanged(index, offset)
            )
        },
        onDeleteGroup = { groupId ->
            // Clear selection if deleting the currently selected group
            if (groupId == selectedGroupId) {
                sharedViewModel.selectGroup(null, null)
            }
            groupsViewModel.onEvent(GroupsUiEvent.DeleteGroup(groupId))
        },
        onManageSubunits = { groupId ->
            navController.navigate(Routes.manageSubunitsRoute(groupId))
        }
    )

}
