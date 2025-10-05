package es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.GroupsScreen
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.viewmodel.ListUserGroupsViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun GroupsFeature(
    listUserGroupsViewModel: ListUserGroupsViewModel = koinViewModel<ListUserGroupsViewModel>()
) {

    val groups = listUserGroupsViewModel.groups.collectAsState()
    val loading = listUserGroupsViewModel.loading.collectAsState()
    val error = listUserGroupsViewModel.error.collectAsState()

    if (error.value != null) {
        Timber.e("Error loading groups: ${error.value}")
    }

    GroupsScreen(
        groups = groups.value,
        loading = loading.value,
        errorMessage = error.value,
        onGroupClicked = { groupId ->
            // handle navigation or callback
        })

}
