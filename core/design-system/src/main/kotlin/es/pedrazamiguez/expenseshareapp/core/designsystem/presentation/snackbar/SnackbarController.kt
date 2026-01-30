package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Controller for showing snackbars that survives navigation between screens.
 *
 * This is necessary because when a feature navigates away (e.g., after creating a group),
 * the feature's coroutine scope is cancelled, which would cancel the snackbar.
 * By using a scope tied to MainScreen, the snackbar persists across navigation.
 */
@Stable
class SnackbarController(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    /**
     * Shows a snackbar that survives navigation.
     * This method returns immediately and doesn't suspend.
     */
    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onResult: ((SnackbarResult) -> Unit)? = null
    ) {
        scope.launch {
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration
            )
            onResult?.invoke(result)
        }
    }
}

/**
 * Creates and remembers a [SnackbarController] with its associated scope.
 * Should be called at the MainScreen level.
 */
@Composable
fun rememberSnackbarController(
    hostState: SnackbarHostState = remember { SnackbarHostState() }
): SnackbarController {
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) {
        SnackbarController(hostState, scope)
    }
}

/**
 * CompositionLocal for providing a [SnackbarController] to child composables.
 *
 * This allows features to show snackbars through the main Scaffold without
 * needing to pass the SnackbarController explicitly through parameters.
 * The snackbar will survive navigation because the scope is tied to MainScreen.
 *
 * Usage in Feature:
 * ```
 * val snackbarController = LocalSnackbarController.current
 *
 * LaunchedEffect(Unit) {
 *     viewModel.actions.collectLatest { action ->
 *         when (action) {
 *             is UiAction.ShowMessage -> {
 *                 snackbarController.showSnackbar(
 *                     message = action.message.asString(context),
 *                     duration = SnackbarDuration.Long
 *                 )
 *             }
 *         }
 *     }
 * }
 * ```
 */
val LocalSnackbarController = compositionLocalOf<SnackbarController> {
    error("No SnackbarController provided. Make sure to wrap your content with a provider that sets LocalSnackbarController.")
}
