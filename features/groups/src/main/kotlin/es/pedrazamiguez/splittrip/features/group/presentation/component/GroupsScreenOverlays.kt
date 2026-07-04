package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CircleCheck
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Edit
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Lock
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Sitemap
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Trash
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.X
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.ActionBottomSheet
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.SheetAction
import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel

@Suppress("LongParameterList")
@Composable
internal fun GroupsScreenOverlays(
    selectedGroup: GroupUiModel?,
    selectedGroupId: String?,
    isSoleGroup: Boolean,
    currentUserId: String?,
    onSelectGroup: (groupId: String, groupName: String, currency: String) -> Unit,
    onEditGroup: (String) -> Unit,
    onManageSubunits: (String) -> Unit,
    onMenuDismiss: () -> Unit,
    onDeleteRequested: (GroupUiModel) -> Unit,
    onArchiveRequested: (GroupUiModel) -> Unit,
    onLeaveRequested: (GroupUiModel) -> Unit
) {
    selectedGroup?.let { group ->
        val isActive = group.id == selectedGroupId
        val selectActionText = if (isActive) {
            stringResource(R.string.action_deselect_group)
        } else {
            stringResource(R.string.action_select_active_group)
        }
        val endTripText = stringResource(DesignSystemR.string.group_detail_end_trip)
        val deleteGroupText = stringResource(R.string.action_delete_group)
        val leaveGroupText = stringResource(R.string.action_leave_group)
        val editGroupText = stringResource(R.string.action_edit_group)
        val manageSubunitsText = stringResource(R.string.action_manage_subunits)

        val selectAction = selectActionForGroup(
            group = group,
            isActive = isActive,
            isSoleGroup = isSoleGroup,
            text = selectActionText,
            onSelectGroup = onSelectGroup,
            onMenuDismiss = onMenuDismiss
        )
        val ownerActions = ownerActionsForGroup(
            group = group,
            currentUserId = currentUserId,
            endTripText = endTripText,
            deleteGroupText = deleteGroupText,
            onDeleteRequested = onDeleteRequested,
            onArchiveRequested = onArchiveRequested
        )
        val leaveAction = leaveActionForGroup(
            group = group,
            currentUserId = currentUserId,
            text = leaveGroupText,
            onLeaveRequested = onLeaveRequested,
            onMenuDismiss = onMenuDismiss
        )

        ActionBottomSheet(
            title = stringResource(R.string.group_actions_title, group.name),
            icon = TablerIcons.Outline.UsersGroup,
            actions = sheetActionsForGroup(
                group = group,
                selectAction = selectAction,
                ownerActions = ownerActions,
                leaveAction = leaveAction,
                editGroupText = editGroupText,
                manageSubunitsText = manageSubunitsText,
                onEditGroup = onEditGroup,
                onManageSubunits = onManageSubunits,
                onMenuDismiss = onMenuDismiss
            ),
            onDismiss = onMenuDismiss
        )
    }
}

private fun selectActionForGroup(
    group: GroupUiModel,
    isActive: Boolean,
    isSoleGroup: Boolean,
    text: String,
    onSelectGroup: (groupId: String, groupName: String, currency: String) -> Unit,
    onMenuDismiss: () -> Unit
): SheetAction? {
    if (isActive && isSoleGroup) return null
    val icon = if (isActive) TablerIcons.Outline.X else TablerIcons.Outline.CircleCheck
    return SheetAction(
        text = text,
        icon = icon,
        onClick = {
            onSelectGroup(group.id, group.name, group.currency)
            onMenuDismiss()
        }
    )
}

private fun ownerActionsForGroup(
    group: GroupUiModel,
    currentUserId: String?,
    endTripText: String,
    deleteGroupText: String,
    onDeleteRequested: (GroupUiModel) -> Unit,
    onArchiveRequested: (GroupUiModel) -> Unit
): List<SheetAction> {
    if (group.status != GroupStatus.ACTIVE || group.createdBy != currentUserId) return emptyList()
    return listOf(
        SheetAction(
            text = endTripText,
            icon = TablerIcons.Outline.Lock,
            onClick = { onArchiveRequested(group) },
            isDestructive = true
        ),
        SheetAction(
            text = deleteGroupText,
            icon = TablerIcons.Outline.Trash,
            onClick = { onDeleteRequested(group) },
            isDestructive = true
        )
    )
}

private fun leaveActionForGroup(
    group: GroupUiModel,
    currentUserId: String?,
    text: String,
    onLeaveRequested: (GroupUiModel) -> Unit,
    onMenuDismiss: () -> Unit
): SheetAction? {
    if (group.status != GroupStatus.ACTIVE || group.createdBy == currentUserId || currentUserId == null) return null
    return SheetAction(
        text = text,
        icon = TablerIcons.Outline.X,
        onClick = {
            onLeaveRequested(group)
            onMenuDismiss()
        },
        isDestructive = true
    )
}

@Suppress("LongParameterList")
private fun sheetActionsForGroup(
    group: GroupUiModel,
    selectAction: SheetAction?,
    ownerActions: List<SheetAction>,
    leaveAction: SheetAction?,
    editGroupText: String,
    manageSubunitsText: String,
    onEditGroup: (String) -> Unit,
    onManageSubunits: (String) -> Unit,
    onMenuDismiss: () -> Unit
): List<SheetAction> = listOfNotNull(
    selectAction,
    SheetAction(
        text = editGroupText,
        icon = TablerIcons.Outline.Edit,
        onClick = {
            onEditGroup(group.id)
            onMenuDismiss()
        }
    ).takeIf { group.status == GroupStatus.ACTIVE },
    SheetAction(
        text = manageSubunitsText,
        icon = TablerIcons.Outline.Sitemap,
        onClick = {
            onManageSubunits(group.id)
            onMenuDismiss()
        }
    ),
    leaveAction
) + ownerActions
