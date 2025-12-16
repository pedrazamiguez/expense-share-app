package es.pedrazamiguez.expenseshareapp.features.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.screen.GroupsScreen
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.ListUserGroupsViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun GroupsFeature(
    listUserGroupsViewModel: ListUserGroupsViewModel = koinViewModel<ListUserGroupsViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel<SharedViewModel>()
) {
    val navController = LocalTabNavController.current

    val groups by listUserGroupsViewModel.groups.collectAsState()
    val loading by listUserGroupsViewModel.loading.collectAsState()
    val error by listUserGroupsViewModel.error.collectAsState()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsState()

    if (error != null) {
        Timber.e("Error loading groups: $error")
    }

    GroupsScreen(
        groups = groups,
        loading = loading,
        errorMessage = error,
        selectedGroupId = selectedGroupId,
        onGroupClicked = { groupId ->
            if (groupId != selectedGroupId) {
                sharedViewModel.selectGroup(groupId)
            } else {
                sharedViewModel.selectGroup(null)
            }
        },
        onCreateGroupClick = {
            navController.navigate(Routes.CREATE_GROUP)
        }
    )

}
