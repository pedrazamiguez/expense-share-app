package es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.GroupsScreen
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.viewmodel.ListUserGroupsViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun GroupsFeature(
    listUserGroupsViewModel: ListUserGroupsViewModel = koinViewModel<ListUserGroupsViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel<SharedViewModel>()
) {

    val groups = listUserGroupsViewModel.groups.collectAsState()
    val loading = listUserGroupsViewModel.loading.collectAsState()
    val error = listUserGroupsViewModel.error.collectAsState()
    val selectedGroupId = sharedViewModel.selectedGroupId.collectAsState()

    if (error.value != null) {
        Timber.e("Error loading groups: ${error.value}")
    }

    GroupsScreen(
        groups = groups.value,
        loading = loading.value,
        errorMessage = error.value,
        selectedGroupId = selectedGroupId.value,
        onGroupClicked = { groupId ->
            if (groupId != selectedGroupId.value) {
                sharedViewModel.selectGroup(groupId)
            } else {
                sharedViewModel.selectGroup(null)
            }
        })

}
