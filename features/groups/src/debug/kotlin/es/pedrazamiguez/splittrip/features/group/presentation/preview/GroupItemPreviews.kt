package es.pedrazamiguez.splittrip.features.group.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupItem

@PreviewLocales
@Composable
private fun GroupItemPreview() {
    GroupUiPreviewHelper {
        GroupItem(groupUiModel = it)
    }
}

@PreviewLocales
@Composable
private fun GroupItemSelectedPreview() {
    GroupUiPreviewHelper {
        GroupItem(groupUiModel = it, isSelected = true)
    }
}
