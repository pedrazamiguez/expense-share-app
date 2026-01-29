package es.pedrazamiguez.expenseshareapp.features.group.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.MappedPreview
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel

@Composable
fun GroupUiPreviewHelper(
    domainGroup: Group = GROUP_DOMAIN_1,
    content: @Composable (GroupUiModel) -> Unit
) {
    MappedPreview(
        domain = domainGroup,
        mapper = GroupU
    )
}
