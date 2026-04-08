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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val GRADIENT_BUTTON_HEIGHT = 56.dp
private val GRADIENT_BUTTON_ELEVATION = 8.dp
private val LOADING_INDICATOR_SIZE = 24.dp
private val LOADING_INDICATOR_STROKE_WIDTH = 2.dp

private const val DISABLED_CONTAINER_ALPHA = 0.12f
private const val DISABLED_CONTENT_ALPHA = 0.38f

/**
 * A primary CTA button implementing the Horizon Narrative gradient style (§5 Buttons).
 *
 * Renders a full-pill button with a linear gradient fill from `colorScheme.primary`
 * to `colorScheme.primaryContainer`, as defined in the Horizon Narrative design spec.
 * Because the gradient derives entirely from [MaterialTheme.colorScheme] tokens, it adapts to
 * the active theme automatically — no separate dark-mode logic is needed.
 *
 * Built as a plain [Box] with [clickable] and [Modifier.shadow] — follows the same proven
 * shadow + clip + background pattern used by the bottom navigation bar. Intentionally avoids
 * Material `Button`/`Surface` composables whose internal `graphicsLayer` can occlude the
 * platform shadow.
 *
 * Features:
 * - **Shape:** [CircleShape] (full pill roundedness).
 * - **Background:** linear gradient `primary → primaryContainer` via [Modifier.background].
 * - **Shadow:** platform elevation shadow via [Modifier.shadow] with default colours.
 * - **Disabled state:** gradient removed; container uses `onSurface` at [DISABLED_CONTAINER_ALPHA]
 *   opacity and content uses `onSurface` at [DISABLED_CONTENT_ALPHA] opacity (Material 3 standard).
 * - **Loading state:** displays a [CircularProgressIndicator] in `onPrimary` colour and
 *   disables interaction.
 * - **Text style:** [androidx.compose.material3.Typography.titleSmall] with [FontWeight.Bold].
 *
 * @param text      The label displayed on the button.
 * @param onClick   Called when the user taps the button. Only fires when [enabled] is `true`
 *                  and [isLoading] is `false`.
 * @param modifier  Optional [Modifier] applied to the button container. Callers are expected to
 *                  supply width constraints (e.g. `Modifier.fillMaxWidth()`) and any surrounding
 *                  padding here. The button height is managed internally.
 * @param enabled   Whether the button is interactive. When `false`, the gradient is replaced
 *                  with a disabled visual and tap events are suppressed.
 * @param isLoading When `true`, replaces the text label with a [CircularProgressIndicator]
 *                  and prevents user interaction.
 * @param leadingIcon Optional icon displayed to the left of [text]. When `null` only the
 *                    text label is shown.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val isClickEnabled = enabled && !isLoading
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val backgroundModifier = if (isClickEnabled) {
        Modifier.background(
            brush = Brush.linearGradient(colors = listOf(primary, primaryContainer)),
            shape = CircleShape
        )
    } else {
        Modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA),
            shape = CircleShape
        )
    }

    val contentColor = if (isClickEnabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    }

    val interactionSource = remember { MutableInteractionSource() }

    // shadow → clip → background → clickable
    // Exact same modifier chain as the bottom navigation bar pill.
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(GRADIENT_BUTTON_HEIGHT)
            .shadow(
                elevation = if (isClickEnabled) GRADIENT_BUTTON_ELEVATION else 0.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .then(backgroundModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.onPrimary),
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
                        tint = contentColor
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
