package es.pedrazamiguez.splittrip.features.group.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.component.DisputeSettlementBottomSheet
import es.pedrazamiguez.splittrip.features.group.presentation.component.SettlementContent
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event.GroupSettlementOverviewUiEvent
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupSettlementOverviewUiState

@Composable
fun GroupSettlementOverviewScreen(
    uiState: GroupSettlementOverviewUiState,
    onEvent: (GroupSettlementOverviewUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.activeDisputeSettlementId != null) {
        DisputeSettlementBottomSheet(
            reason = uiState.disputeReasonInput,
            onReasonChanged = { onEvent(GroupSettlementOverviewUiEvent.DisputeReasonChanged(it)) },
            onSubmit = { onEvent(GroupSettlementOverviewUiEvent.DisputeSubmitted) },
            onDismiss = { onEvent(GroupSettlementOverviewUiEvent.DisputeCancelled) }
        )
    }

    when {
        uiState.isLoading -> ShimmerLoadingList()
        uiState.hasError -> EmptyStateView(
            title = stringResource(R.string.settlement_overview_error_loading)
        )
        else -> SettlementContent(uiState = uiState, onEvent = onEvent, modifier = modifier)
    }
}
