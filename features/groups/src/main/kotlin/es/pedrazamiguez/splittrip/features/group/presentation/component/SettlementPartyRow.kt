package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowRight
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel

@Composable
internal fun SettlementPartyRow(
    settlement: SettlementRowUiModel,
    modifier: Modifier = Modifier
) {
    val debtorColor = if (settlement.isCurrentUserDebtor) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val creditorColor = if (settlement.isCurrentUserCreditor) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = settlement.debtorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = boldIfCurrent(settlement.isCurrentUserDebtor),
                color = debtorColor
            )
            Icon(
                TablerIcons.Outline.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settlement.creditorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = boldIfCurrent(settlement.isCurrentUserCreditor),
                color = creditorColor
            )
        }
        Text(
            text = settlement.formattedAmount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun boldIfCurrent(isCurrentUser: Boolean): FontWeight =
    if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
