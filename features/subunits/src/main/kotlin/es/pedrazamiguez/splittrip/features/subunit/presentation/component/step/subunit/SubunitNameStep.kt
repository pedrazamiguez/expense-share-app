package es.pedrazamiguez.splittrip.features.subunit.presentation.component.step.subunit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import es.pedrazamiguez.splittrip.core.designsystem.extension.asString
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.rememberAutoFocusRequester
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.subunit.R
import es.pedrazamiguez.splittrip.features.subunit.presentation.viewmodel.event.CreateEditSubunitUiEvent
import es.pedrazamiguez.splittrip.features.subunit.presentation.viewmodel.state.CreateEditSubunitUiState

/**
 * Step 1: Subunit name input with auto-focus.
 */
@Composable
fun SubunitNameStep(
    uiState: CreateEditSubunitUiState,
    onEvent: (CreateEditSubunitUiEvent) -> Unit,
    onImeNext: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nameFocusRequester = rememberAutoFocusRequester()
    val focusManager = LocalFocusManager.current

    WizardStepLayout(modifier = modifier) {
        StyledOutlinedTextField(
            value = uiState.name,
            onValueChange = { onEvent(CreateEditSubunitUiEvent.UpdateName(it)) },
            label = stringResource(R.string.subunit_field_name),
            placeholder = stringResource(R.string.subunit_field_name_hint),
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.asString(),
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onImeNext()
                }
            ),
            focusRequester = nameFocusRequester,
            moveCursorToEndOnFocus = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
