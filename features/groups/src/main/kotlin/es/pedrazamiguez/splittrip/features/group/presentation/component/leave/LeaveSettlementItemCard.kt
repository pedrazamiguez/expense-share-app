package es.pedrazamiguez.splittrip.features.group.presentation.component.leave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSettlementUiModel

@Composable
fun LeaveSettlementItemCard(
    settlement: LeaveSettlementUiModel,
    onConfirmClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlatCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
        ) {
            LeaveSettlementCardHeader(
                directionTitle = settlement.directionTitle,
                pocketType = settlement.pocketType,
                pocketTypeLabel = settlement.pocketTypeLabel
            )

            LeaveSettlementCardFooter(
                formattedAmount = settlement.formattedAmount,
                canCurrentUserConfirm = settlement.canCurrentUserConfirm,
                statusType = settlement.statusType,
                statusLabel = settlement.statusLabel,
                settlementId = settlement.settlementId,
                onConfirmClicked = onConfirmClicked
            )
        }
    }
}
