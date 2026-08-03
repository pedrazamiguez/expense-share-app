package es.pedrazamiguez.splittrip.features.settlement.presentation.feature

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.features.settlement.presentation.component.DisputeReasonDialog
import es.pedrazamiguez.splittrip.features.settlement.presentation.component.YourPositionContent
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.state.YourPositionUiState

@Composable
internal fun YourPositionFeatureBody(
    uiState: YourPositionUiState,
    onEvent: (YourPositionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val personalPosition = uiState.personalPosition ?: return
    YourPositionContent(
        personalPosition = personalPosition,
        isCashBreakdownVisible = uiState.isCashBreakdownVisible,
        settlementConsensus = uiState.settlementConsensus,
        chart = uiState.spendingChart,
        isCashOnly = uiState.isChartCashOnly,
        isOffline = uiState.isOffline,
        onEvent = onEvent,
        modifier = modifier.fillMaxSize()
    )

    if (uiState.activeDisputeSettlementId != null) {
        DisputeReasonDialog(
            reason = uiState.disputeReasonInput,
            onReasonChanged = { reason -> onEvent(YourPositionUiEvent.DisputeReasonChanged(reason)) },
            onSubmit = { onEvent(YourPositionUiEvent.DisputeSubmitted) },
            onDismiss = { onEvent(YourPositionUiEvent.DisputeCancelled) },
            isOffline = uiState.isOffline
        )
    }
}
