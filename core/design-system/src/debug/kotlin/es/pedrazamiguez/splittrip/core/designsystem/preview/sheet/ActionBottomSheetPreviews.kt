package es.pedrazamiguez.splittrip.core.designsystem.preview.sheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.ActionBottomSheet
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.SheetAction
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun ActionBottomSheetDefaultPreview() {
    PreviewThemeWrapper {
        ActionBottomSheet(
            title = stringResource(R.string.preview_sheet_group_title, "Summer Trip 2026"),
            icon = Icons.Outlined.Groups,
            actions = listOf(
                SheetAction(
                    text = stringResource(R.string.preview_action_edit_group),
                    icon = Icons.Outlined.Edit,
                    onClick = {}
                ),
                SheetAction(
                    text = stringResource(R.string.preview_action_delete_group),
                    icon = Icons.Outlined.Delete,
                    onClick = {},
                    isDestructive = true
                )
            ),
            onDismiss = {}
        )
    }
}

@PreviewComplete
@Composable
private fun ActionBottomSheetMultipleActionsPreview() {
    PreviewThemeWrapper {
        ActionBottomSheet(
            title = stringResource(R.string.preview_sheet_expense_title, "Dinner at restaurant"),
            actions = listOf(
                SheetAction(
                    text = stringResource(R.string.action_edit),
                    icon = Icons.Outlined.Edit,
                    onClick = {}
                ),
                SheetAction(
                    text = stringResource(R.string.preview_action_share),
                    icon = Icons.Outlined.Share,
                    onClick = {}
                ),
                SheetAction(
                    text = stringResource(R.string.action_delete),
                    icon = Icons.Outlined.Delete,
                    onClick = {},
                    isDestructive = true
                )
            ),
            onDismiss = {}
        )
    }
}

@PreviewComplete
@Composable
private fun ActionBottomSheetWithDisabledActionPreview() {
    PreviewThemeWrapper {
        ActionBottomSheet(
            title = stringResource(R.string.preview_sheet_group_title, "Beach Vacation"),
            icon = Icons.Outlined.Groups,
            actions = listOf(
                SheetAction(
                    text = stringResource(R.string.preview_action_edit_group),
                    icon = Icons.Outlined.Edit,
                    onClick = {},
                    enabled = false
                ),
                SheetAction(
                    text = stringResource(R.string.preview_action_delete_group),
                    icon = Icons.Outlined.Delete,
                    onClick = {},
                    isDestructive = true
                )
            ),
            onDismiss = {}
        )
    }
}
