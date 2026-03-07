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

    // When isLoading becomes true, wait [showDelay] before actually showing loading UI
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Reset in case a previous isLoading=false coroutine was cancelled mid-hold
            holdingMinDisplay = false
            delay(showDelay)
            showLoading = true
            loadingShownAt = System.currentTimeMillis()
        } else {
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
        // isLoading == true but showLoading == false → still within showDelay → render nothing
        // (blank frame, imperceptible for short delays)
    }
}

