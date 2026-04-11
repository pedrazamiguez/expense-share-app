package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private const val GHOST_BORDER_ALPHA_LIGHT = 0.15f
private const val GHOST_BORDER_ALPHA_DARK = 0.22f
private const val DARK_THEME_LUMINANCE_THRESHOLD = 0.5f

/**
 * A flat card container with zero elevation that achieves visual depth through
 * tonal layering rather than explicit borders (Horizon Narrative §2 "No-Line Rule").
 *
 * The default [color] is `surfaceContainerLow`, which sits naturally **darker / more
 * tinted** than the off-white page background (`surface` / `background` token) — no
 * stroke needed (the Layering Principle). Cards feel grounded and slightly inset
 * rather than floating as bright white panels.
 *
 * Use this as the standard card wrapper across the entire app to guarantee
 * visual consistency. Override [shape] or [color] only when the design
 * explicitly calls for a variation (e.g., `shapes.medium` for nested cards,
 * or `primaryContainer` for a selected state).
 *
 * @param modifier    Outer modifier applied to the [Surface].
 * @param shape       Card corner shape. Defaults to `MaterialTheme.shapes.large`.
 * @param color       Background color. Defaults to `surfaceContainerLow`
 *                    (Layering Principle inset tier — slightly tinted relative to
 *                    the off-white page background in light mode; lighter than the
 *                    near-black background in dark mode).
 * @param ghostBorder When `true`, draws an `outlineVariant` border at reduced opacity
 *                    (15% light / 22% dark). Reserved for edge cases where two adjacent
 *                    identical-colour surfaces make tonal contrast alone insufficient —
 *                    typically dark-mode scenarios. Defaults to `false`.
 * @param content     The card content slot.
 */
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    ghostBorder: Boolean = false,
    content: @Composable () -> Unit
) {
    val border = if (ghostBorder) {
        val isDark = MaterialTheme.colorScheme.surface.luminance() < DARK_THEME_LUMINANCE_THRESHOLD
        val alpha = if (isDark) GHOST_BORDER_ALPHA_DARK else GHOST_BORDER_ALPHA_LIGHT
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha))
    } else {
        null
    }
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        border = border,
        content = content
    )
}
