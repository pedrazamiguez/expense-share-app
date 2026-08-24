package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.features.settings.presentation.component.SubscriptionsContent
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.event.SubscriptionsUiEvent
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.state.SubscriptionsUiState

@Composable
fun SubscriptionsScreen(
    uiState: SubscriptionsUiState,
    onEvent: (SubscriptionsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    DeferredLoadingContainer(
        isLoading = uiState.isLoading,
        loadingContent = { ShimmerLoadingList() }
    ) {
        SubscriptionsContent(
            uiState = uiState,
            onEvent = onEvent,
            modifier = modifier
        )
    }
}
