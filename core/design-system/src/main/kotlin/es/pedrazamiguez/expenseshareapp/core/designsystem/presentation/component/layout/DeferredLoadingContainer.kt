package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import es.pedrazamiguez.expenseshareapp.core.designsystem.constant.UiConstants
import kotlinx.coroutines.delay

/**
 * A container that prevents "flash of loading state" for fast responses.
 *
 * **Problem:** When data loads quickly (< 150ms), showing a shimmer skeleton and
 * immediately replacing it with content creates an ugly flicker.
 *
 * **Solution:** This composable delays the appearance of loading content by [showDelay].
 * If the loading state ends before the delay expires, the loading content is never shown
 * at all — the user sees a blank screen briefly, then the real content, which feels instant.
 *
 * Once the loading content *does* appear, it stays visible for at least [minDisplayTime]
 * to prevent a jarring flash (skeleton appearing for just a frame or two).
 *
 * **Visual continuity on reload (within the same composition):** When content was
 * previously displayed and a reload starts (`isLoading` transitions from `false` to `true`),
 * the previous content remains visible during the [showDelay] window instead of rendering
 * a blank frame. This smooths over brief reloads such as pull-to-refresh or a `stateIn`
 * resubscribe while the user stays on the same screen.
 *
 * This is scoped to the lifetime of this composable instance; if the composable is removed
 * from the composition (for example, when a tab's content is disposed on tab switch), its
 * internal state is reset when it is recreated. Cross-tab visual continuity is handled at
 * the flow layer via `FLOW_REPLAY_EXPIRATION`, which resets the `stateIn` cache to
 * `initialValue` (typically `isLoading = true`) so that the first frame after recreation
 * enters the blank/shimmer path rather than flashing stale content.
 *
 * @param isLoading Whether the data is currently loading.
 * @param showDelay Delay (ms) before showing the loading content. Default: [UiConstants.LOADING_SHOW_DELAY_MS].
 * @param minDisplayTime Minimum time (ms) the loading content stays visible once shown.
 *   Default: [UiConstants.LOADING_MIN_DISPLAY_TIME_MS].
 * @param loadingContent The loading UI (e.g., [ShimmerLoadingList]).
 * @param content The real content shown when loading completes.
 */
@Composable
fun DeferredLoadingContainer(
    isLoading: Boolean,
    showDelay: Long = UiConstants.LOADING_SHOW_DELAY_MS,
    minDisplayTime: Long = UiConstants.LOADING_MIN_DISPLAY_TIME_MS,
    loadingContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    // Whether the loading UI is actually visible on screen
    var showLoading by remember { mutableStateOf(false) }

    // Track when loading content became visible (for min display time)
    var loadingShownAt by remember { mutableStateOf(0L) }

    // Whether we must keep showing loading to satisfy minDisplayTime
    var holdingMinDisplay by remember { mutableStateOf(false) }

    // Whether content has ever been rendered (used for visual continuity on reload)
    var hasShownContent by remember { mutableStateOf(!isLoading) }

    // When isLoading becomes true, wait [showDelay] before actually showing loading UI
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Reset in case a previous isLoading=false coroutine was cancelled mid-hold
            holdingMinDisplay = false
            delay(showDelay)
            showLoading = true
            loadingShownAt = System.currentTimeMillis()
        } else {
            // Content is about to be shown — mark for visual continuity on future reloads
            hasShownContent = true
            // Loading just ended — check if we need to hold loading UI for minDisplayTime
            if (showLoading && loadingShownAt > 0L) {
                val elapsed = System.currentTimeMillis() - loadingShownAt
                val remaining = minDisplayTime - elapsed
                if (remaining > 0) {
                    holdingMinDisplay = true
                    delay(remaining)
                }
            }
            holdingMinDisplay = false
            showLoading = false
            loadingShownAt = 0L
        }
    }

    when {
        showLoading || holdingMinDisplay -> loadingContent()
        !isLoading -> content()
        // isLoading == true but showLoading == false → still within showDelay.
        // If content was previously shown (same composition instance), keep rendering it
        // for visual continuity. Note: content() renders the *current* uiState, which may
        // already have empty data if the upstream emitted a loading-empty state. This is a
        // conscious tradeoff — 150ms of stale/empty content is less jarring than a blank
        // frame, and the shimmer will replace it once showDelay expires.
        // On first-ever load (or after composable recreation), hasShownContent is false,
        // so this branch does not fire and the blank frame is preserved.
        hasShownContent -> content()
    }
}
