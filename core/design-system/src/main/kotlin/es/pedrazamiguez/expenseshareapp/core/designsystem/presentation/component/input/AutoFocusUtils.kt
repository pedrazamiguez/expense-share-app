package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.FocusRequester

/**
 * Creates and returns a [FocusRequester] that automatically requests focus
 * **once** on the first composition (when [autoFocus] is `true`).
 *
 * A [rememberSaveable] guard ensures the focus request does not re-fire
 * after configuration changes (e.g. rotation).
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
 *                  (e.g. [AmountCurrencyCardState.autoFocus]).
 */
@Composable
fun rememberAutoFocusRequester(autoFocus: Boolean = true): FocusRequester {
    val focusRequester = remember { FocusRequester() }
    val hasRequestedFocus = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(autoFocus) {
        if (autoFocus && !hasRequestedFocus.value) {
            focusRequester.requestFocus()
            hasRequestedFocus.value = true
        }
    }

    return focusRequester
}
