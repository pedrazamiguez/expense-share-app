package es.pedrazamiguez.splittrip.features.balance.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CategorySpendingUiModel
import kotlin.math.roundToInt

@Composable
fun CategorySpendingItemRow(
    item: CategorySpendingUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(item.color, CircleShape)
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.Medium))

        // Icon
        Icon(
            imageVector = item.categoryIcon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.Medium))

        // Category name and percentage
        val percentage = (item.progress * 100).roundToInt()
        Text(
            text = "${item.categoryName} ($percentage%)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        // Amount
        Text(
            text = item.formattedAmount,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
