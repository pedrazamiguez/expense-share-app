package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.core.designsystem.transition.fabSharedTransitionModifier

/** Bottom clearance so the drop shadow stays within the composable's layout bounds. */
private val SHADOW_CLEARANCE_BOTTOM = 8.dp

/** Top clearance for the subtle ambient shadow above the button. */
private val SHADOW_CLEARANCE_TOP = 2.dp

/**
 * A full-width, pill-shaped primary CTA anchored at the bottom of the screen.
 *
 * Renders with the Horizon Narrative gradient fill (`primary → primaryContainer`),
 * matching the primary button tier (§5 Buttons). Supports shared element transitions.
 *
 * **Shadow clearance:** Tab screens render inside `NavHost`'s `AnimatedContent`, which
 * clips content to its layout bounds. [GradientButton]'s [Modifier.shadow] draws the
 * shadow outside the button's bounds — without extra room, the shadow is clipped.
 * The inner [SHADOW_CLEARANCE_BOTTOM]/[SHADOW_CLEARANCE_TOP] padding reserves layout
 * space so the shadow renders within the composable's own bounds. Callers should
 * account for this extra height in their bottom padding (typically reduce by
 * [SHADOW_CLEARANCE_BOTTOM]).
 *
 * Delegates all gradient, shadow, and loading visuals to [GradientButton].
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
    val sharedModifier = if (sharedTransitionKey != null) {
        fabSharedTransitionModifier(sharedTransitionKey)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(sharedModifier)
            .padding(bottom = SHADOW_CLEARANCE_BOTTOM, top = SHADOW_CLEARANCE_TOP),
        contentAlignment = Alignment.Center
    ) {
        GradientButton(
            text = text,
            leadingIcon = icon,
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
