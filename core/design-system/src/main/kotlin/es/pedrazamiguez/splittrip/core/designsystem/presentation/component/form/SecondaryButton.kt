package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

private val SECONDARY_BUTTON_HEIGHT = 56.dp
private val SECONDARY_BUTTON_ELEVATION = 4.dp
private const val DISABLED_CONTAINER_ALPHA = 0.12f
private const val DISABLED_CONTENT_ALPHA = 0.38f

/**
 * A secondary-tier button following the Horizon Narrative design spec.
 *
 * Visually lighter than [GradientButton] but shares the same pill shape, height,
 * and Box-based shadow pattern. Uses `surfaceContainerHigh` as the container colour
 * with `onSurface` content — appropriate for secondary actions such as "Back" or
 * "Cancel".
 *
 * @param text         The label displayed on the button.
 * @param onClick      Called when the user taps the button (only fires when [enabled]).
 * @param modifier     Optional [Modifier] for width/padding. Height is managed internally.
 * @param enabled      Whether the button is interactive.
 * @param leadingIcon  Optional icon to the left of [text].
 * @param trailingIcon Optional icon to the right of [text].
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA)
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(SECONDARY_BUTTON_HEIGHT)
            .shadow(
                elevation = if (enabled) SECONDARY_BUTTON_ELEVATION else 0.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(color = containerColor, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.onSurface),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        ButtonContentRow(text, contentColor, leadingIcon, trailingIcon)
    }
}
