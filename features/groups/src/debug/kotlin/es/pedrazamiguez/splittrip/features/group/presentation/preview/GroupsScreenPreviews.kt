package es.pedrazamiguez.splittrip.features.group.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.features.group.presentation.screen.GroupsScreen
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupsUiState

@PreviewComplete
@Composable
private fun GroupsScreenLoadingPreview() {
    PreviewThemeWrapper {
        GroupsScreen(
            uiState = GroupsUiState(isLoading = true)
        )
    }
}

@PreviewComplete
@Composable
private fun GroupsScreenEmptyPreview() {
    PreviewThemeWrapper {
        GroupsScreen(
            uiState = GroupsUiState(isLoading = false)
        )
    }
}

@PreviewComplete
@Composable
private fun GroupsScreenWithDataPreview() {
    GroupsScreenPreviewHelper {
        GroupsScreen(
            uiState = GroupsUiState(
                isLoading = false,
                groups = it
            ),
            selectedGroupId = GROUP_DOMAIN_1.id
        )
    }
}
