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
@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(resId, *args)
    }
}
