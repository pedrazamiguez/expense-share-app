package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event.GroupSettlementOverviewUiEvent
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupSettlementOverviewUiState

@Composable
internal fun SettlementContent(
    uiState: GroupSettlementOverviewUiState,
    onEvent: (GroupSettlementOverviewUiEvent) -> Unit,
    modifier: Modifier = Modifier
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
