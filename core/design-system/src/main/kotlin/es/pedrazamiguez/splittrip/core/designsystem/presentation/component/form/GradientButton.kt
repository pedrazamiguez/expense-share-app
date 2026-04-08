package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val GRADIENT_BUTTON_HEIGHT = 56.dp
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
 * Features:
 * - **Shape:** [CircleShape] (full pill roundedness).
 * - **Background:** linear gradient `primary → primaryContainer` via [Modifier.background].
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
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
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
        Modifier
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(backgroundModifier)
            .height(GRADIENT_BUTTON_HEIGHT),
        enabled = isClickEnabled,
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
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(LOADING_INDICATOR_SIZE),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = LOADING_INDICATOR_STROKE_WIDTH
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
