package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.balance.presentation.model.StatusChipStyle

@Composable
internal fun SettlementStatusChip(
    label: String,
    style: StatusChipStyle,
    modifier: Modifier = Modifier
) {
    val chipContainerColor = when (style) {
        StatusChipStyle.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        StatusChipStyle.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        StatusChipStyle.ERROR -> MaterialTheme.colorScheme.errorContainer
        StatusChipStyle.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val chipContentColor = when (style) {
        StatusChipStyle.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        StatusChipStyle.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        StatusChipStyle.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        StatusChipStyle.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = chipContainerColor,
        contentColor = chipContentColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.Small,
                vertical = MaterialTheme.spacing.ExtraSmall
            )
        )
    }
}
