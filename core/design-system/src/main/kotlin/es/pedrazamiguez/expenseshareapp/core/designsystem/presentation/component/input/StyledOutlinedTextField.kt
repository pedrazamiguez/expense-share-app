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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
 * @param focusRequester Optional [FocusRequester] to programmatically request focus on this field
 * @param moveCursorToEndOnFocus When `true`, the cursor is moved to the end of the text whenever
 *                               the field gains focus programmatically (via [focusRequester]).
 *                               Internally uses [TextFieldValue] to control selection. Use together
 *                               with [focusRequester] + [rememberAutoFocusRequester] so that when
 *                               the user navigates back to a step that already has text, the cursor
 *                               lands at the end rather than the beginning.
 * @param focusable Whether the text field can receive focus. Set to `false` for dropdown triggers
 *                  that should not steal focus from an adjacent editable field. Defaults to `true`.
 * @param shape The shape of the text field's border
 * @param colors Custom colors for the text field
 */
@Suppress("LongMethod", "LongParameterList") // Compose UI builder DSL — not procedural logic
@Composable
fun StyledOutlinedTextField(
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
    focusRequester: FocusRequester? = null,
    moveCursorToEndOnFocus: Boolean = false,
    focusable: Boolean = true,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = appOutlinedTextFieldColors()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val effectiveModifier = if (focusRequester != null) {
        modifier.focusRequester(focusRequester)
    } else {
        modifier
    }
    val focusModifier = if (!focusable) {
        Modifier.focusProperties { canFocus = false }
    } else {
        Modifier
    }

    val fieldConfig = TextFieldConfig(
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
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

    when {
        readOnly && onClick != null -> ReadOnlyClickableTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = effectiveModifier,
            focusModifier = focusModifier,
            config = fieldConfig,
            onClick = onClick
        )
        moveCursorToEndOnFocus -> CursorToEndTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = effectiveModifier,
            focusModifier = focusModifier,
            config = fieldConfig
        )
        else -> StandardTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = effectiveModifier,
            focusModifier = focusModifier,
            config = fieldConfig
        )
    }
}

/**
 * Bundles the common [OutlinedTextField] visual/behavioral parameters so they
 * can be forwarded to the three internal variants without repeating each one.
 */
private data class TextFieldConfig(
    val label: String?,
    val placeholder: String?,
    val leadingIcon: @Composable (() -> Unit)?,
    val trailingIcon: @Composable (() -> Unit)?,
    val prefix: @Composable (() -> Unit)?,
    val suffix: @Composable (() -> Unit)?,
    val supportingText: String?,
    val isError: Boolean,
    val enabled: Boolean,
    val readOnly: Boolean,
    val singleLine: Boolean,
    val maxLines: Int,
    val minLines: Int,
    val visualTransformation: VisualTransformation,
    val keyboardOptions: KeyboardOptions,
    val keyboardActions: KeyboardActions,
    val interactionSource: MutableInteractionSource,
    val shape: Shape,
    val colors: TextFieldColors
)

/**
 * Read-only variant wrapped in a clickable overlay — used for dropdown triggers.
 */
@Composable
private fun ReadOnlyClickableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    focusModifier: Modifier,
    config: TextFieldConfig,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        CoreOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().then(focusModifier),
            config = config.copy(readOnly = true)
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
}

/**
 * Variant that uses [TextFieldValue] internally to move the cursor to the end
 * when the field gains programmatic focus.
 */
@Composable
private fun CursorToEndTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    focusModifier: Modifier,
    config: TextFieldConfig
) {
    var internalTfv by remember { mutableStateOf(TextFieldValue(value)) }
    // Sync external value changes (e.g. ViewModel clears or resets the field).
    // Guard against overriding the cursor while the user is actively typing.
    LaunchedEffect(value) {
        if (internalTfv.text != value) {
            internalTfv = TextFieldValue(value, TextRange(value.length))
        }
    }
    OutlinedTextField(
        value = internalTfv,
        onValueChange = { newTfv ->
            internalTfv = newTfv
            onValueChange(newTfv.text)
        },
        modifier = modifier
            .then(focusModifier)
            .onFocusChanged { focusState ->
                if (focusState.isFocused &&
                    internalTfv.selection.start == 0 &&
                    internalTfv.text.isNotEmpty()
                ) {
                    internalTfv = internalTfv.copy(
                        selection = TextRange(internalTfv.text.length)
                    )
                }
            },
        label = config.label?.let { { Text(it) } },
        placeholder = config.placeholder?.let { { Text(it) } },
        leadingIcon = config.leadingIcon,
        trailingIcon = config.trailingIcon,
        prefix = config.prefix,
        suffix = config.suffix,
        supportingText = config.supportingText?.let { { Text(it) } },
        isError = config.isError,
        enabled = config.enabled,
        readOnly = config.readOnly,
        singleLine = config.singleLine,
        maxLines = config.maxLines,
        minLines = config.minLines,
        visualTransformation = config.visualTransformation,
        keyboardOptions = config.keyboardOptions,
        keyboardActions = config.keyboardActions,
        interactionSource = config.interactionSource,
        shape = config.shape,
        colors = config.colors
    )
}

/**
 * Plain editable text field — the default branch.
 */
@Composable
private fun StandardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    focusModifier: Modifier,
    config: TextFieldConfig
) {
    CoreOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.then(focusModifier),
        config = config
    )
}

/**
 * Shared [OutlinedTextField] call used by [ReadOnlyClickableTextField] and
 * [StandardTextField] — avoids duplicating the full parameter list.
 */
@Composable
private fun CoreOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    config: TextFieldConfig
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = config.label?.let { { Text(it) } },
        placeholder = config.placeholder?.let { { Text(it) } },
        leadingIcon = config.leadingIcon,
        trailingIcon = config.trailingIcon,
        prefix = config.prefix,
        suffix = config.suffix,
        supportingText = config.supportingText?.let { { Text(it) } },
        isError = config.isError,
        enabled = config.enabled,
        readOnly = config.readOnly,
        singleLine = config.singleLine,
        maxLines = config.maxLines,
        minLines = config.minLines,
        visualTransformation = config.visualTransformation,
        keyboardOptions = config.keyboardOptions,
        keyboardActions = config.keyboardActions,
        interactionSource = config.interactionSource,
        shape = config.shape,
        colors = config.colors
    )
}

/**
 * Returns the default colors for [StyledOutlinedTextField].
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
    errorCursorColor = MaterialTheme.colorScheme.error
)
