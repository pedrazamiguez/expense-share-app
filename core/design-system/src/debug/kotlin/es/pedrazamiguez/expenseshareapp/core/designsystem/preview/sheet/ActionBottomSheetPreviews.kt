package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.sheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.ActionBottomSheet
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.SheetAction
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun ActionBottomSheetDefaultPreview() {
    PreviewThemeWrapper {
        ActionBottomSheet(
            title = "Group \"Summer Trip 2026\"",
            icon = Icons.Outlined.Groups,
            actions = listOf(
                SheetAction(
                    text = "Edit group",
                    icon = Icons.Outlined.Edit,
                    onClick = {}
                ),
                SheetAction(
                    text = "Delete group",
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
            title = "Expense \"Dinner at restaurant\"",
            actions = listOf(
                SheetAction(
                    text = "Edit",
                    icon = Icons.Outlined.Edit,
                    onClick = {}
                ),
                SheetAction(
                    text = "Share",
                    icon = Icons.Outlined.Share,
                    onClick = {}
                ),
                SheetAction(
                    text = "Delete",
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
            title = "Group \"Beach Vacation\"",
            icon = Icons.Outlined.Groups,
            actions = listOf(
                SheetAction(
                    text = "Edit group",
                    icon = Icons.Outlined.Edit,
                    onClick = {},
                    enabled = false
                ),
                SheetAction(
                    text = "Delete group",
                    icon = Icons.Outlined.Delete,
                    onClick = {},
                    isDestructive = true
                )
            ),
            onDismiss = {}
        )
    }
}
