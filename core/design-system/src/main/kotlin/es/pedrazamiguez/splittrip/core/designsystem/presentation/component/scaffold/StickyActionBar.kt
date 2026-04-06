package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.transition.fabSharedTransitionModifier

private val STICKY_BAR_HEIGHT = 56.dp
private val STICKY_BAR_SHAPE = RoundedCornerShape(28.dp)

/**
 * A full-width, rounded action button anchored at the bottom of the screen.
 *
 * Modern replacement for [ExpressiveFab] on list screens. It sits flush above
 * the bottom navigation area, feels grounded and intentional, and supports
 * shared element transitions via [sharedTransitionKey].
 *
 * @param text The button label.
 * @param icon Leading icon for the button.
 * @param onClick Callback when pressed.
 * @param modifier Modifier (typically includes alignment + padding).
 * @param enabled Whether the button is enabled. Defaults to `true`.
 * @param containerColor Background color. Defaults to `tertiary`.
 * @param contentColor Text/icon color. Defaults to `onTertiary`.
 * @param sharedTransitionKey Optional key for container-transform shared element transitions.
 */
@Composable
fun StickyActionBar(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    sharedTransitionKey: String? = null
) {
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
            .then(sharedModifier),
        enabled = enabled,
        shape = STICKY_BAR_SHAPE,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
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
