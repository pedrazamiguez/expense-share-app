package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

/** Small delay so the TextField's internal TextFieldValue is initialised before focus. */
private const val FOCUS_REQUEST_DELAY_MS = 50L

/**
 * Creates and returns a [FocusRequester] that automatically requests focus
 * **once** on the first composition (when [autoFocus] is `true`).
 *
 * A [rememberSaveable] guard ensures the focus request does not re-fire
 * after configuration changes (e.g. rotation).
 *
 * A small delay is applied before requesting focus so the hosting
 * `OutlinedTextField` has time to initialise its internal `TextFieldValue`
 * with the correct cursor position (end of text).
 *
 * Pass the returned [FocusRequester] to a text field's `focusRequester` parameter.
 *
 * ```
 * val focusRequester = rememberAutoFocusRequester()
 * StyledOutlinedTextField(
 *     ...,
 *     focusRequester = focusRequester
 * )
 * ```
 *
 * @param autoFocus When `false`, the [FocusRequester] is still created but
 *                  focus is never requested. This is useful for components
 *                  where auto-focus is toggled via a state flag
 *                  (e.g. `AmountCurrencyCardState.autoFocus`).
 */
@Composable
fun rememberAutoFocusRequester(autoFocus: Boolean = true): FocusRequester {
    val focusRequester = remember { FocusRequester() }
    val hasRequestedFocus = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(autoFocus) {
        if (autoFocus && !hasRequestedFocus.value) {
            delay(FOCUS_REQUEST_DELAY_MS)
            focusRequester.requestFocus()
            hasRequestedFocus.value = true
        }
    }

    return focusRequester
}
