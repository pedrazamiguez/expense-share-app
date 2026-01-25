package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for TopAppBarScrollBehavior that can be shared between
 * the top bar (in Scaffold) and content (in NavHost).
 */
@OptIn(ExperimentalMaterial3Api::class)
class TopAppBarScrollBehaviorState {
    var scrollBehavior: TopAppBarScrollBehavior? by mutableStateOf(null)
        internal set
}

/**
 * CompositionLocal to provide TopAppBarScrollBehaviorState.
 * This enables scroll-aware top app bars in screens using ScreenUiProvider pattern.
 */
val LocalTopAppBarState = compositionLocalOf { TopAppBarScrollBehaviorState() }

/**
 * CompositionLocal to provide TopAppBarScrollBehavior directly.
 * Use LocalTopAppBarState for more control, or this for simple access.
 */
@OptIn(ExperimentalMaterial3Api::class)
val LocalTopAppBarScrollBehavior = compositionLocalOf<TopAppBarScrollBehavior?> { null }

/**
 * Creates and remembers a TopAppBarScrollBehaviorState.
 */
@Composable
fun rememberTopAppBarState(): TopAppBarScrollBehaviorState {
    return remember { TopAppBarScrollBehaviorState() }
}

/**
 * Creates an exitUntilCollapsed scroll behavior and registers it with the LocalTopAppBarState.
 * Call this in your screen content to enable scroll-aware top app bar.
 *
 * @return The scroll behavior to use with nestedScroll modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberConnectedScrollBehavior(): TopAppBarScrollBehavior {
    val state = LocalTopAppBarState.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Register this scroll behavior with the state holder
    state.scrollBehavior = scrollBehavior

    return scrollBehavior
}

/**
 * Provides a TopAppBarScrollBehaviorState to child composables.
 * Use this at a high level (e.g., in MainScreen) to enable scroll behavior sharing.
 */
@Composable
fun ProvideTopAppBarState(
    state: TopAppBarScrollBehaviorState = rememberTopAppBarState(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTopAppBarState provides state) {
        content()
    }
}

/**
 * Provides a TopAppBarScrollBehavior to child composables via CompositionLocal.
 * Use this in your screen content to connect scroll behavior to the top app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvideTopAppBarScrollBehavior(
    scrollBehavior: TopAppBarScrollBehavior,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTopAppBarScrollBehavior provides scrollBehavior) {
        content()
    }
}

