package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NavigationBarIcon(
    icon: ImageVector,
    contentDescription: String?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val iconTint = if (tint != Color.Unspecified) {
        tint
    } else if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = iconTint,
        modifier = modifier.size(24.dp)
    )
}
