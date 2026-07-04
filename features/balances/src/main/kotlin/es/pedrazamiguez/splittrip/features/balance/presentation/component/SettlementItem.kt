package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowRight
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.features.balance.presentation.model.SettlementUiModel

@Composable
fun SettlementItem(
    settlement: SettlementUiModel,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isInvolved = settlement.isCurrentUserDebtor || settlement.isCurrentUserCreditor
    val cardColor = if (isInvolved) colorScheme.surfaceContainerLowest else colorScheme.surfaceContainer
    FlatCard(modifier = modifier.fillMaxWidth(), color = cardColor) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
        ) {
            if (isInvolved) SettlementItemBadge(isDebtor = settlement.isCurrentUserDebtor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettlementPocketTypeChip(label = settlement.pocketTypeLabel)
                SettlementStatusChip(label = settlement.statusLabel, style = settlement.statusChipStyle)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        color = debtColor(settlement.isCurrentUserDebtor, colorScheme.error, colorScheme.onSurface)
                    )
                    Icon(TablerIcons.Outline.ArrowRight, null, tint = colorScheme.onSurfaceVariant)
                    Text(
                        text = settlement.creditorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = boldIfCurrent(settlement.isCurrentUserCreditor),
                        color = creditColor(
                            settlement.isCurrentUserCreditor,
                            colorScheme.primary,
                            colorScheme.onSurface
                        )
                    )
                }
                Text(
                    text = "${settlement.currencyCode} ${settlement.formattedAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

private fun boldIfCurrent(isCurrentUser: Boolean): FontWeight =
    if (isCurrentUser) FontWeight.Bold else FontWeight.Normal

private fun debtColor(isCurrentUser: Boolean, highlight: Color, normal: Color): Color =
    if (isCurrentUser) highlight else normal

private fun creditColor(isCurrentUser: Boolean, highlight: Color, normal: Color): Color =
    if (isCurrentUser) highlight else normal
