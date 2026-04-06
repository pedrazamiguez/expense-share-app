package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * Remembers whether a FAB should be visible based on the scroll direction of a [LazyListState].
 *
 * - **Visible** when the user is scrolling up, idle, or when the list is at the very top.
 * - **Hidden** when the user is actively scrolling down (new content appearing from below).
 *
 * Uses [snapshotFlow] to observe scroll position changes in a [LaunchedEffect], avoiding
 * side-effect mutations inside snapshot-read scopes.
 *
 * **Important:** Do not wrap the FAB in `AnimatedVisibility` — that removes the composable
 * from composition and breaks shared element transitions. Use [ScrollAwareFabContainer] instead,
 * which keeps the FAB always composed and animates via `graphicsLayer`.
 *
 * @param listState The [LazyListState] driving the list whose scroll direction is observed.
 * @return `true` when the FAB should be visible, `false` when it should be hidden.
 */
@Composable
fun rememberScrollAwareFabVisibility(listState: LazyListState): Boolean {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (currentIndex, currentOffset) ->
            val scrollingDown = currentIndex > previousIndex ||
                (currentIndex == previousIndex && currentOffset > previousOffset)

            previousIndex = currentIndex
            previousOffset = currentOffset

            isVisible = !scrollingDown || (currentIndex == 0 && currentOffset == 0)
        }
    }

    return isVisible
}

/**
 * A container that keeps its FAB content **always composed** (preserving shared element
 * transitions) while smoothly animating alpha and vertical translation based on scroll
 * direction.
 *
 * Unlike `AnimatedVisibility`, this never removes the FAB from the composition tree,
 * so shared element return animations always find their target.
 *
 * When hidden, the container applies [clearAndSetSemantics] to remove the FAB from the
 * accessibility tree and prevent ghost interactions.
 *
 * @param listState The [LazyListState] observed for scroll direction.
 * @param modifier Modifier applied to the container (typically alignment and padding).
 * @param visible Additional visibility condition (e.g., `!uiState.isLoading`).
 *                Combined with scroll-aware visibility via logical AND.
 * @param content The FAB composable to render inside.
 */
@Composable
fun ScrollAwareFabContainer(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val scrollVisible = rememberScrollAwareFabVisibility(listState)
    val shouldShow = scrollVisible && visible

    val alpha by animateFloatAsState(
        targetValue = if (shouldShow) 1f else 0f,
        label = "scroll-fab-alpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (shouldShow) 0f else FAB_HIDE_TRANSLATION_Y,
        label = "scroll-fab-translation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY
            }
            .then(if (shouldShow) Modifier else Modifier.clearAndSetSemantics {})
    ) {
        content()
    }
}

private const val FAB_HIDE_TRANSLATION_Y = 200f
