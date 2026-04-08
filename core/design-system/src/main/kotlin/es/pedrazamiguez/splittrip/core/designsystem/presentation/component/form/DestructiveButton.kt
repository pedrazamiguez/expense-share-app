package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DESTRUCTIVE_BUTTON_HEIGHT = 56.dp
private val DESTRUCTIVE_BUTTON_ELEVATION = 6.dp
private val LOADING_INDICATOR_SIZE = 24.dp
private val LOADING_INDICATOR_STROKE_WIDTH = 2.dp
private const val DISABLED_CONTAINER_ALPHA = 0.12f
private const val DISABLED_CONTENT_ALPHA = 0.38f

/**
 * A destructive-action button following the Horizon Narrative design spec.
 *
 * Uses `errorContainer` / `onErrorContainer` colours for dangerous actions such as
 * "Logout" or "Delete". Shares the same pill shape, height, and Box-based shadow
 * pattern as [GradientButton] and [SecondaryButton].
 *
 * Built as a plain [Box] with [Modifier.shadow] → [Modifier.clip] → [Modifier.background]
 * → [clickable], intentionally avoiding Material `Button`/`Surface` whose internal
 * `graphicsLayer` can occlude the platform shadow.
 *
 * @param text      The label displayed on the button.
 * @param onClick   Called when the user taps the button (only fires when [enabled]
 *                  and [isLoading] is `false`).
 * @param modifier  Optional [Modifier] for width/padding. Height is managed internally.
 * @param enabled   Whether the button is interactive.
 * @param isLoading When `true`, replaces the label with a spinner and prevents interaction.
 * @param leadingIcon Optional icon displayed to the left of [text].
 */
@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val isClickEnabled = enabled && !isLoading

    val containerColor = if (isClickEnabled) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA)
    }

    val contentColor = if (isClickEnabled) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(DESTRUCTIVE_BUTTON_HEIGHT)
            .shadow(
                elevation = if (isClickEnabled) DESTRUCTIVE_BUTTON_ELEVATION else 0.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(color = containerColor, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.onErrorContainer),
                enabled = isClickEnabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(LOADING_INDICATOR_SIZE),
                color = contentColor,
                strokeWidth = LOADING_INDICATOR_STROKE_WIDTH
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = if (leadingIcon != null) {
                        Modifier.padding(start = 8.dp)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}
