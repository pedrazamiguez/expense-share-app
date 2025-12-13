package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NavigationBarIcon(
    icon: ImageVector,
    contentDescription: String?,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1f)

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
        modifier = modifier.size(24.dp * scale)
    )
}
