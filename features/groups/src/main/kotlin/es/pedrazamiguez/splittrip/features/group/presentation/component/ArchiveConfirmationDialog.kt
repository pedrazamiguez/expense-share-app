package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel

@Composable
internal fun ArchiveConfirmationDialog(
    groupToArchive: GroupUiModel?,
    onArchiveGroup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    groupToArchive?.let { group ->
        DestructiveConfirmationDialog(
            title = stringResource(DesignSystemR.string.group_detail_end_trip_title),
            text = stringResource(DesignSystemR.string.group_detail_end_trip_message, group.name),
            confirmLabel = stringResource(DesignSystemR.string.group_detail_end_trip_confirm),
            onDismiss = onDismiss,
            onConfirm = {
                onArchiveGroup(group.id)
                onDismiss()
            }
        )
    }
}
