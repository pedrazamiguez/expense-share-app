package es.pedrazamiguez.splittrip.features.group.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.pedrazamiguez.splittrip.core.designsystem.constant.UiConstants
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedElementAnimation
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.StickyActionBar
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.ActionBottomSheet
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.SheetAction
import es.pedrazamiguez.splittrip.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.splittrip.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupItem
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupsUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun GroupsScreen(
    uiState: GroupsUiState = GroupsUiState(),
    selectedGroupId: String? = null,
    onGroupClicked: (groupId: String, groupName: String) -> Unit = { _, _ -> },
    onCreateGroupClick: () -> Unit = {},
    onScrollPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    onDeleteGroup: (groupId: String) -> Unit = {},
    onManageSubunits: (groupId: String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    var hasRestoredScroll by remember { mutableStateOf(false) }
    var selectedGroupForMenu by remember { mutableStateOf<GroupUiModel?>(null) }
    var groupToDelete by remember { mutableStateOf<GroupUiModel?>(null) }

    GroupsScrollEffects(
        uiState = uiState,
        listState = listState,
        hasRestoredScroll = hasRestoredScroll,
        onScrollPositionChanged = onScrollPositionChanged,
        onScrollRestored = { hasRestoredScroll = true }
    )

    GroupsScreenContent(
        uiState = uiState,
        selectedGroupId = selectedGroupId,
        listState = listState,
        onCreateGroupClick = onCreateGroupClick,
        onGroupClicked = onGroupClicked,
        onGroupLongClicked = { selectedGroupForMenu = it }
    )

    GroupsScreenOverlays(
        selectedGroup = selectedGroupForMenu,
        groupToDelete = groupToDelete,
        onDeleteGroup = onDeleteGroup,
        onManageSubunits = onManageSubunits,
        onMenuDismiss = { selectedGroupForMenu = null },
        onDeleteRequested = { group ->
            groupToDelete = group
            selectedGroupForMenu = null
        },
        onDeleteDismiss = { groupToDelete = null }
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun GroupsScrollEffects(
    uiState: GroupsUiState,
    listState: LazyListState,
    hasRestoredScroll: Boolean,
    onScrollPositionChanged: (Int, Int) -> Unit,
    onScrollRestored: () -> Unit
) {
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && !hasRestoredScroll && uiState.groups.isNotEmpty()) {
            if (uiState.scrollPosition > 0 || uiState.scrollOffset > 0) {
                listState.scrollToItem(uiState.scrollPosition, uiState.scrollOffset)
            }
            onScrollRestored()
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .debounce(UiConstants.SCROLL_POSITION_DEBOUNCE_MS)
            .collect { (index, offset) -> onScrollPositionChanged(index, offset) }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GroupsScreenContent(
    uiState: GroupsUiState,
    selectedGroupId: String?,
    listState: LazyListState,
    onCreateGroupClick: () -> Unit,
    onGroupClicked: (String, String) -> Unit,
    onGroupLongClicked: (GroupUiModel) -> Unit
) {
    val bottomPadding = LocalBottomPadding.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            DeferredLoadingContainer(
                isLoading = uiState.isLoading,
                loadingContent = { ShimmerLoadingList() }
            ) {
                when {
                    uiState.groups.isEmpty() -> {
                        EmptyStateView(title = stringResource(R.string.groups_not_found), icon = Icons.Outlined.Groups)
                    }
                    else -> {
                        GroupsListContent(
                            groups = uiState.groups,
                            selectedGroupId = selectedGroupId,
                            listState = listState,
                            bottomPadding = bottomPadding,
                            onGroupClicked = onGroupClicked,
                            onGroupLongClicked = onGroupLongClicked
                        )
                    }
                }
            }
            StickyActionBar(
                text = stringResource(R.string.groups_create),
                icon = Icons.Outlined.Add,
                onClick = onCreateGroupClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = bottomPadding + 12.dp),
                sharedTransitionKey = CREATE_GROUP_SHARED_ELEMENT_KEY
            )
        }
    }
}

@Composable
private fun GroupsScreenOverlays(
    selectedGroup: GroupUiModel?,
    groupToDelete: GroupUiModel?,
    onDeleteGroup: (String) -> Unit,
    onManageSubunits: (String) -> Unit,
    onMenuDismiss: () -> Unit,
    onDeleteRequested: (GroupUiModel) -> Unit,
    onDeleteDismiss: () -> Unit
) {
    selectedGroup?.let { group ->
        ActionBottomSheet(
            title = stringResource(R.string.group_actions_title, group.name),
            icon = Icons.Outlined.Groups,
            actions = listOf(
                SheetAction(
                    text = stringResource(R.string.action_edit_group),
                    icon = Icons.Outlined.Edit,
                    onClick = { onMenuDismiss() }
                ),
                SheetAction(
                    text = stringResource(R.string.action_manage_subunits),
                    icon = Icons.Outlined.AccountTree,
                    onClick = {
                        onManageSubunits(group.id)
                        onMenuDismiss()
                    }
                ),
                SheetAction(
                    text = stringResource(R.string.action_delete_group),
                    icon = Icons.Outlined.Delete,
                    onClick = { onDeleteRequested(group) },
                    isDestructive = true
                )
            ),
            onDismiss = onMenuDismiss
        )
    }

    groupToDelete?.let { group ->
        DestructiveConfirmationDialog(
            title = stringResource(R.string.group_delete_title),
            text = stringResource(R.string.group_delete_warning, group.name),
            onDismiss = onDeleteDismiss,
            onConfirm = {
                onDeleteGroup(group.id)
                onDeleteDismiss()
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GroupsListContent(
    groups: ImmutableList<GroupUiModel>,
    selectedGroupId: String?,
    listState: LazyListState,
    bottomPadding: Dp,
    onGroupClicked: (String, String) -> Unit,
    onGroupLongClicked: (GroupUiModel) -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val fabExtraPadding = 72.dp // Space for StickyActionBar
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomPadding + fabExtraPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            Column {
                Text(
                    text = stringResource(R.string.groups_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.groups_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(items = groups, key = { it.id }) { group ->
            GroupItem(
                modifier = Modifier
                    .animateItem()
                    .sharedElementAnimation(
                        key = "group-${group.id}",
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                groupUiModel = group,
                isSelected = group.id == selectedGroupId,
                onClick = onGroupClicked,
                onLongClick = { onGroupLongClicked(group) }
            )
        }
    }
}
