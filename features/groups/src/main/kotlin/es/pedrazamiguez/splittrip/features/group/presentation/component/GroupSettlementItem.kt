package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel

@Composable
internal fun GroupSettlementItem(
    settlement: SettlementRowUiModel,
    onConfirm: () -> Unit,
    onDispute: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlatCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GroupSettlementStatusChip(
                    label = settlement.statusLabel,
                    style = settlement.statusChipStyle
                )
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = settlement.pocketTypeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.Small,
                            vertical = MaterialTheme.spacing.ExtraSmall
                        )
                    )
                }
            }

            SettlementPartyRow(settlement = settlement)

            if (settlement.status == SettlementStatus.DISPUTED) {
                SettlementDisputeBanner(reason = settlement.disputeReason)
            }

            if (settlement.canCurrentUserConfirm || settlement.canCurrentUserDispute) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))
                SettlementActionButtons(
                    canConfirm = settlement.canCurrentUserConfirm,
                    canDispute = settlement.canCurrentUserDispute,
                    onConfirm = onConfirm,
                    onDispute = onDispute
                )
            }
        }
    }
}
