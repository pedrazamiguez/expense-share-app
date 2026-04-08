package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.transition.fabSharedTransitionModifier

private val STICKY_BAR_HEIGHT = 56.dp

private const val DISABLED_CONTAINER_ALPHA = 0.12f
private const val DISABLED_CONTENT_ALPHA = 0.38f

/**
 * A full-width, pill-shaped primary CTA anchored at the bottom of the screen.
 *
 * Renders with the Horizon Narrative gradient fill (`primary → primaryContainer`),
 * matching the primary button tier (§5 Buttons). Supports shared element transitions.
 *
 * @param text The button label.
 * @param icon Leading icon for the button.
 * @param onClick Callback when pressed.
 * @param modifier Modifier applied by the host screen (typically alignment + padding).
 * @param enabled Whether the button is enabled. Defaults to `true`.
 * @param sharedTransitionKey Optional key for container-transform shared element transitions.
 */
@Composable
fun StickyActionBar(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sharedTransitionKey: String? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val backgroundModifier = if (enabled) {
        Modifier.background(
            brush = Brush.linearGradient(colors = listOf(primary, primaryContainer)),
            shape = CircleShape
        )
    } else {
        Modifier
    }

    val sharedModifier = if (sharedTransitionKey != null) {
        fabSharedTransitionModifier(sharedTransitionKey)
    } else {
        Modifier
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(STICKY_BAR_HEIGHT)
            .then(backgroundModifier)
            .then(sharedModifier),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
