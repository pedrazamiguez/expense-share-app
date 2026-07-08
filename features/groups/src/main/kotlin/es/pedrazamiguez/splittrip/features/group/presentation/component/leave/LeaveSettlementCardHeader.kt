package es.pedrazamiguez.splittrip.features.group.presentation.component.leave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType

@Composable
fun LeaveSettlementCardHeader(
    directionTitle: String,
    pocketType: SettlementPocketType,
    pocketTypeLabel: String,
    modifier: Modifier = Modifier
) {
    val (chipBg, chipFg) = getPocketTypeColors(pocketType, MaterialTheme.colorScheme)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = directionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = chipBg,
            contentColor = chipFg,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(start = MaterialTheme.spacing.Small)
        ) {
            Text(
                text = pocketTypeLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private fun getPocketTypeColors(
    pocketType: SettlementPocketType,
    colorScheme: ColorScheme
) = when (pocketType) {
    SettlementPocketType.POCKET ->
        colorScheme.primaryContainer to colorScheme.onPrimaryContainer
    SettlementPocketType.CASH ->
        colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
    SettlementPocketType.NET ->
        colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
}
