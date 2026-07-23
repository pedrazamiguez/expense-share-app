package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ReceiptRefund
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementConsensusItemUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun SettlementConsensusSection(
    settlements: ImmutableList<SettlementConsensusItemUiModel>,
    onConfirm: (settlementId: String) -> Unit,
    onDispute: (settlementId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        Text(
            text = stringResource(R.string.your_position_settlement_consensus_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (settlements.isEmpty()) {
            EmptyStateView(
                icon = TablerIcons.Outline.ReceiptRefund,
                title = stringResource(R.string.your_position_settlement_empty_title),
                description = stringResource(R.string.your_position_settlement_empty_description),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            settlements.forEach { item ->
                SettlementConsensusCard(
                    item = item,
                    onConfirm = { onConfirm(item.settlementId) },
                    onDispute = { onDispute(item.settlementId) }
                )
            }
        }
    }
}
