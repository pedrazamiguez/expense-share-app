package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A flat card container with zero elevation, a subtle 1dp border, and a
 * `surfaceContainerLow` background.
 *
 * Use this as the standard card wrapper across the entire app to guarantee
 * visual consistency. Override [shape] or [color] only when the design
 * explicitly calls for a variation (e.g., `shapes.medium` for nested cards,
 * or `primaryContainer` for a selected state).
 *
 * @param modifier Outer modifier applied to the [Surface].
 * @param shape Card corner shape. Defaults to `MaterialTheme.shapes.large`.
 * @param color Background color. Defaults to `surfaceContainerLow`.
 * @param borderColor Border stroke color. Defaults to `outlineVariant`.
 * @param content The card content slot.
 */
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        border = BorderStroke(1.dp, borderColor),
        content = content
    )
}
