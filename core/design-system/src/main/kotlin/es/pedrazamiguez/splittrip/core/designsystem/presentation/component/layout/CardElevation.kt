package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.constant.UiConstants
import es.pedrazamiguez.splittrip.core.designsystem.transition.LocalSharedTransitionScope
import kotlinx.coroutines.delay

private const val SHADOW_FADE_DURATION_MS = 200

/**
 * Returns an animated elevation value for hero cards that participate in
 * [sharedBounds][androidx.compose.animation.ExperimentalSharedTransitionApi] transitions
 * or [animateItem][androidx.compose.foundation.lazy.LazyItemScope.animateItem] placement
 * animations.
 *
 * Handles four edge cases automatically (Horizon Narrative §4.4):
 * - **Dark mode:** always returns `0.dp` — tonal layering takes over, shadows are invisible.
 * - **First frame (`appearedOnce`):** defers to `0.dp` so the full shadow never flashes
 *   while `animateItem()` is still placing the card into its final position. The deferral
 *   window is [UiConstants.ITEM_PLACEMENT_SETTLE_MS] (250 ms) — long enough for the spring
 *   placement animation to substantially settle before the shadow starts growing.
 * - **Active transition (`isTransitionActive`):** snaps to `0.dp` immediately (kill switch)
 *   to prevent a squared-shadow artefact during `sharedBounds` overlay rendering.
 * - **Alpha animation buffer (caller responsibility):** `animateItem()` must be called with
 *   `fadeInSpec = null, fadeOutSpec = null` on any item that uses `FlatCard(elevation > 0)`.
 *   The default alpha fade creates a rectangular offscreen hardware buffer; `FlatCard`'s
 *   `graphicsLayer { clip = false }` shadow bleeds outside its own bounds but is silently
 *   clipped by that rectangular buffer edge — producing the hard squared-shadow artefact.
 *   Disabling the alpha animations eliminates the buffer; the spring placement animation
 *   is retained and unaffected.
 *
 * ### How the transition kill switch works
 *
 * When `isTransitionActive` becomes `true`, the `animateDpAsState` target immediately drops
 * to `0.dp`, but the tween still takes 200 ms to reach zero — exactly the window where the
 * squared-shadow artefact appears. The kill switch bypasses the animation entirely and
 * returns `0.dp` the instant `isTransitionActive` is `true`. When the transition ends,
 * `animatedElevation` has already faded toward zero, so the shadow fades back in smoothly
 * from wherever the animation has reached — no pop.
 *
 * Pass the result directly to [FlatCard]`(elevation = …)`. `FlatCard` handles the outer
 * `Box` + `graphicsLayer` wiring; the caller only needs to supply the animated value.
 *
 * @param targetElevation The elevation to animate toward once the card has settled and no
 *                        transition is active. Typically `8.dp` for hero cards.
 * @return An animated [Dp] value safe to pass to [FlatCard].
 *
 * @see FlatCard
 * @see UiConstants.ITEM_PLACEMENT_SETTLE_MS
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun rememberTransitionAwareElevation(targetElevation: Dp): Dp {
    val isDarkMode = isSystemInDarkTheme()
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val isTransitionActive = sharedTransitionScope?.isTransitionActive ?: false
    var appearedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Delay before allowing the shadow to grow. This prevents the shadow from
        // visually "chasing" the card during its animateItem() spring placement —
        // the card is substantially in its final position before the shadow appears.
        delay(UiConstants.ITEM_PLACEMENT_SETTLE_MS)
        appearedOnce = true
    }

    val animationTarget = when {
        isDarkMode || !appearedOnce || isTransitionActive -> 0.dp
        else -> targetElevation
    }

    val animatedElevation by animateDpAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = SHADOW_FADE_DURATION_MS),
        label = "transition_aware_elevation"
    )

    // Kill switch: snap to 0 immediately when a transition starts, regardless of where the
    // fade-out animation is. This prevents a partially-faded shadow from reaching the
    // sharedBounds overlay with a non-zero value and rendering as a hard rectangle.
    return if (isTransitionActive) 0.dp else animatedElevation
}
