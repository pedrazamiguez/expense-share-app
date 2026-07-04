package es.pedrazamiguez.splittrip.features.group.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.DestructiveButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.LabelText
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.component.DisputeSettlementDialog
import es.pedrazamiguez.splittrip.features.group.presentation.component.GroupSettlementItem
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event.GroupSettlementOverviewUiEvent
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupSettlementOverviewUiState

@Composable
fun GroupSettlementOverviewScreen(
    uiState: GroupSettlementOverviewUiState,
    onEvent: (GroupSettlementOverviewUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.activeDisputeSettlementId != null) {
        DisputeSettlementDialog(
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

@Composable
private fun SettlementContent(
    uiState: GroupSettlementOverviewUiState,
    onEvent: (GroupSettlementOverviewUiEvent) -> Unit,
    modifier: Modifier
) {
    val bottomPadding = LocalBottomPadding.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = MaterialTheme.spacing.Default,
                end = MaterialTheme.spacing.Default,
                top = MaterialTheme.spacing.Small
            ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        SettlementSection(
            labelRes = R.string.settlement_overview_section_pending,
            settlements = uiState.pendingSettlements,
            onConfirm = { id -> onEvent(GroupSettlementOverviewUiEvent.ConfirmSettlement(id)) },
            onDispute = { id -> onEvent(GroupSettlementOverviewUiEvent.DisputeSettlement(id)) }
        )
        SettlementSection(
            labelRes = R.string.settlement_overview_section_disputed,
            settlements = uiState.disputedSettlements
        )
        SettlementSection(
            labelRes = R.string.settlement_overview_section_resolved,
            settlements = uiState.resolvedSettlements
        )
        SettlementEmptyText(
            show = uiState.pendingSettlements.isEmpty() &&
                uiState.disputedSettlements.isEmpty() &&
                uiState.resolvedSettlements.isEmpty()
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Medium))
        SettlementCloseTripButton(
            allResolved = uiState.areAllSettlementsResolved,
            isArchiving = uiState.isArchiving,
            onCloseTrip = { onEvent(GroupSettlementOverviewUiEvent.CloseTripClicked) }
        )
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

@Composable
private fun SettlementSection(
    labelRes: Int,
    settlements: kotlinx.collections.immutable.ImmutableList<SettlementRowUiModel>,
    onConfirm: (String) -> Unit = {},
    onDispute: (String) -> Unit = {}
) {
    if (settlements.isNotEmpty()) {
        LabelText(text = stringResource(labelRes))
        settlements.forEach { settlement ->
            GroupSettlementItem(
                settlement = settlement,
                onConfirm = { onConfirm(settlement.settlementId) },
                onDispute = { onDispute(settlement.settlementId) }
            )
        }
    }
}

@Composable
private fun SettlementEmptyText(show: Boolean) {
    if (show) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))
        Text(
            text = stringResource(R.string.settlement_overview_empty),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettlementCloseTripButton(
    allResolved: Boolean,
    isArchiving: Boolean,
    onCloseTrip: () -> Unit
) {
    if (!allResolved) return

    if (isArchiving) {
        GradientButton(
            text = stringResource(R.string.settlement_overview_closing_trip),
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        DestructiveButton(
            text = stringResource(R.string.settlement_overview_close_trip),
            onClick = onCloseTrip,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
