package es.pedrazamiguez.expenseshareapp.core.designsystem.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

/**
 * Resolves the [UiText] to a string that can be displayed in the UI.
 * Must be called from a Composable context.
 *
 * Usage:
 * ```
 * Text(text = uiState.message.asString())
 * ```
 */
@Suppress("SpreadOperator") // Spread is unavoidable for vararg-to-vararg delegation to stringResource()
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(resId, *args)
}
