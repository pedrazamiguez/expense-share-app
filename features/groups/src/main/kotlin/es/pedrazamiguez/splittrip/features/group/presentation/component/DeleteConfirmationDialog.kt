package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel

@Composable
internal fun DeleteConfirmationDialog(
    groupToDelete: GroupUiModel?,
    onDeleteGroup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    groupToDelete?.let { group ->
        DestructiveConfirmationDialog(
            title = stringResource(R.string.group_delete_title),
            text = stringResource(R.string.group_delete_warning, group.name),
            onDismiss = onDismiss,
            onConfirm = {
                onDeleteGroup(group.id)
                onDismiss()
            }
        )
    }
}
