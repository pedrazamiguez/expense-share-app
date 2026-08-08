package es.pedrazamiguez.splittrip.features.group.presentation.screen

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.group.presentation.component.DeleteConfirmationDialog
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupsScreenContent
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupsScreenOverlays
import es.pedrazamiguez.splittrip.features.group.presentation.component.RestoreScrollEffect
import es.pedrazamiguez.splittrip.features.group.presentation.component.TrackScrollEffect
import es.pedrazamiguez.splittrip.features.group.presentation.component.leave.GroupLeaveWizardSheet
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupsUiState

@Suppress("kotlin:S107", "LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    uiState: GroupsUiState = GroupsUiState(),
    selectedGroupId: String? = null,
    onUpgradeClicked: () -> Unit = {},
    onGroupClicked: (groupId: String, groupName: String, currency: String) -> Unit = { _, _, _ -> },
    onSelectGroup: (groupId: String, groupName: String, currency: String) -> Unit = { _, _, _ -> },
    onScrollPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    onEditGroup: (groupId: String) -> Unit = {},
    onDeleteGroup: (groupId: String) -> Unit = {},
    onManageSubunits: (groupId: String) -> Unit = {},
    onArchiveGroup: (groupId: String) -> Unit = {},
    onLeaveGroup: (groupId: String) -> Unit = {},
    onWizardNextClicked: (String) -> Unit = {},
    onWizardBackClicked: () -> Unit = {},
    onWizardCancelled: () -> Unit = {},
    onConfirmLeave: (String) -> Unit = {},
    onNavigateToYourPosition: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    var selectedGroupForMenu by remember { mutableStateOf<GroupUiModel?>(null) }
    var groupToDelete by remember { mutableStateOf<GroupUiModel?>(null) }
    var groupToLeave by remember { mutableStateOf<GroupUiModel?>(null) }
    val scrollBehavior = rememberConnectedScrollBehavior()

    RestoreScrollEffect(listState, uiState)

    TrackScrollEffect(listState, onScrollPositionChanged)

    val bottomPadding = LocalBottomPadding.current

    GroupsScreenContent(
        uiState = uiState,
        selectedGroupId = selectedGroupId,
        listState = listState,
        bottomPadding = bottomPadding,
        onGroupClicked = onGroupClicked,
        onGroupLongClicked = { selectedGroupForMenu = it },
        onUpgradeClicked = onUpgradeClicked,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    )

    GroupsScreenOverlays(
        selectedGroup = selectedGroupForMenu,
        selectedGroupId = selectedGroupId,
        isSoleGroup = uiState.groups.size == 1,
        currentUserId = uiState.currentUserId,
        isLeaving = uiState.isLeaving,
        onSelectGroup = onSelectGroup,
        onEditGroup = onEditGroup,
        onManageSubunits = onManageSubunits,
        onMenuDismiss = { selectedGroupForMenu = null },
        onDeleteRequested = { group ->
            groupToDelete = group
            selectedGroupForMenu = null
        },
        onArchiveRequested = { group ->
            selectedGroupForMenu = null
            onArchiveGroup(group.id)
        },
        onLeaveRequested = { group ->
            groupToLeave = group
            selectedGroupForMenu = null
            onLeaveGroup(group.id)
        }
    )

    DeleteConfirmationDialog(groupToDelete, onDeleteGroup) { groupToDelete = null }

    val shouldClearGroupToLeave = !uiState.leaveWizardState.showSheet &&
        groupToLeave != null &&
        !uiState.isLeaving &&
        !uiState.leaveWizardState.isLoading

    if (uiState.leaveWizardState.showSheet && groupToLeave != null) {
        GroupLeaveWizardSheet(
            groupName = groupToLeave!!.name,
            leaveWizardState = uiState.leaveWizardState,
            onNextClicked = { onWizardNextClicked(groupToLeave!!.id) },
            onBackClicked = onWizardBackClicked,
            onDismissRequest = {
                onWizardCancelled()
                groupToLeave = null
            },
            onConfirmLeave = { onConfirmLeave(groupToLeave!!.id) },
            onGoToSettlementsClicked = onNavigateToYourPosition
        )
    } else if (shouldClearGroupToLeave) {
        // Clear groupToLeave when the sheet is closed and not loading
        groupToLeave = null
    }
}
