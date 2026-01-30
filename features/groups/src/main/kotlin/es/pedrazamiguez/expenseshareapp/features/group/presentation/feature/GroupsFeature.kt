package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.GroupsScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.GroupsViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.GroupsUiEvent
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupsFeature(
    groupsViewModel: GroupsViewModel = koinViewModel<GroupsViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel<SharedViewModel>()
) {
    val navController = LocalTabNavController.current

    val uiState by groupsViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        groupsViewModel.onEvent(GroupsUiEvent.LoadGroups)
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
        }
    )

}
