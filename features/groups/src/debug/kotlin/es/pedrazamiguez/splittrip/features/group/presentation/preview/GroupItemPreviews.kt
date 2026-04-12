package es.pedrazamiguez.splittrip.features.group.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupItem

@PreviewLocales
@Composable
private fun GroupItemPreview() {
    GroupUiPreviewHelper(domainGroup = GROUP_DOMAIN_2) {
        GroupItem(groupUiModel = it)
    }
}

@PreviewLocales
@Composable
private fun GroupItemNoImagePreview() {
    GroupUiPreviewHelper(domainGroup = GROUP_DOMAIN_NO_IMAGE) {
        GroupItem(groupUiModel = it)
    }
}
