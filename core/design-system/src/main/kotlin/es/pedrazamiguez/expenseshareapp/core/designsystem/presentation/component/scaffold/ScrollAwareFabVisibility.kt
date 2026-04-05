package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Remembers whether a FAB should be visible based on the scroll direction of a [LazyListState].
 *
 * - **Visible** when the user is scrolling up, idle, or when the list is at the very top.
 * - **Hidden** when the user is actively scrolling down (new content appearing from below).
 *
 * Consumers should wrap their FAB in an `AnimatedVisibility` using the returned value
 * to smoothly show/hide the FAB on scroll.
 *
 * @param listState The [LazyListState] driving the list whose scroll direction is observed.
 * @return `true` when the FAB should be visible, `false` when it should be hidden.
 */
@Composable
fun rememberScrollAwareFabVisibility(listState: LazyListState): Boolean {
    var previousIndex by remember { mutableIntStateOf(listState.firstVisibleItemIndex) }
    var previousOffset by remember { mutableIntStateOf(listState.firstVisibleItemScrollOffset) }

    val isVisible by remember {
        derivedStateOf {
            val currentIndex = listState.firstVisibleItemIndex
            val currentOffset = listState.firstVisibleItemScrollOffset

            val scrollingDown = currentIndex > previousIndex ||
                (currentIndex == previousIndex && currentOffset > previousOffset)

            previousIndex = currentIndex
            previousOffset = currentOffset

            // Show FAB when scrolling up, at the top, or idle
            !scrollingDown || (currentIndex == 0 && currentOffset == 0)
        }
    }

    return isVisible
}
