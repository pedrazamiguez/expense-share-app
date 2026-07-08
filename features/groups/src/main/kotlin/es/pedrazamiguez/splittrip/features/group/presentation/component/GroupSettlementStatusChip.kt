package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowStatusStyle

@Composable
internal fun GroupSettlementStatusChip(
    label: String,
    style: SettlementRowStatusStyle,
    modifier: Modifier = Modifier
) {
    val chipContainerColor = when (style) {
        SettlementRowStatusStyle.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        SettlementRowStatusStyle.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        SettlementRowStatusStyle.ERROR -> MaterialTheme.colorScheme.errorContainer
        SettlementRowStatusStyle.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val chipContentColor = when (style) {
        SettlementRowStatusStyle.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        SettlementRowStatusStyle.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        SettlementRowStatusStyle.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        SettlementRowStatusStyle.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
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
