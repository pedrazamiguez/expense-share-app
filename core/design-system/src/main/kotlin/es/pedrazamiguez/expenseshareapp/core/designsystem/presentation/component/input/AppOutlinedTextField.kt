package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A styled OutlinedTextField component that provides consistent styling across the app.
 *
 * This component wraps Material 3's OutlinedTextField with app-specific defaults and
 * additional convenience features like:
 * - Consistent color scheme using MaterialTheme
 * - Built-in error state handling with optional supporting text
 * - Read-only mode with optional click handling (useful for dropdown triggers)
 * - Configurable keyboard options and actions
 *
 * @param value The input text to be shown in the text field
 * @param onValueChange The callback that is triggered when the input value changes
 * @param modifier Modifier to be applied to the text field
 * @param label The optional label to be displayed inside the text field container
 * @param placeholder The optional placeholder to be displayed when the text field is in focus and empty
 * @param leadingIcon The optional leading icon to be displayed at the beginning of the text field
 * @param trailingIcon The optional trailing icon to be displayed at the end of the text field
 * @param prefix The optional prefix text to be displayed before the input
 * @param suffix The optional suffix text to be displayed after the input
 * @param supportingText The optional supporting text to be displayed below the text field
 * @param isError Whether the text field's current value is in error state
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only (no editing but can be focused)
 * @param singleLine Whether the text field should be a single line
 * @param maxLines Maximum number of visible lines
 * @param minLines Minimum number of visible lines
 * @param visualTransformation Transforms the visual representation of the input
 * @param keyboardType The type of keyboard to show
 * @param imeAction The IME action to show
 * @param capitalization The capitalization behavior
 * @param keyboardActions Keyboard actions to perform
 * @param onClick Optional click handler for read-only fields (e.g., dropdown triggers)
 * @param shape The shape of the text field's border
 * @param colors Custom colors for the text field
 */
@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onClick: (() -> Unit)? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = appOutlinedTextFieldColors(),
) {
    val interactionSource = remember { MutableInteractionSource() }

    // For read-only fields that need click handling (like dropdowns),
    // we wrap with a clickable overlay
    if (readOnly && onClick != null) {
        Box(modifier = modifier) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = label?.let { { Text(it) } },
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText?.let { { Text(it) } },
                isError = isError,
                enabled = enabled,
                readOnly = true,
                singleLine = singleLine,
                maxLines = maxLines,
                minLines = minLines,
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction,
                    capitalization = capitalization
                ),
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                shape = shape,
                colors = colors
            )
            // Invisible overlay to capture click without ripple
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick
                    )
            )
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText?.let { { Text(it) } },
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
                capitalization = capitalization
            ),
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors
        )
    }
}

/**
 * Returns the default colors for [AppOutlinedTextField].
 *
 * These colors are designed to maintain consistent styling across the app
 * while properly supporting read-only states (e.g., for dropdown triggers).
 */
@Composable
fun appOutlinedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    // Standard colors use Material defaults
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error,

    // Text colors
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    errorTextColor = MaterialTheme.colorScheme.onSurface,

    // Label colors
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorLabelColor = MaterialTheme.colorScheme.error,

    // Disabled colors that look like enabled (for read-only dropdown triggers)
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledBorderColor = MaterialTheme.colorScheme.outline,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

    // Cursor and selection
    cursorColor = MaterialTheme.colorScheme.primary,
    errorCursorColor = MaterialTheme.colorScheme.error,
)
