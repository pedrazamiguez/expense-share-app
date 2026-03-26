package es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateEditSubunitUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitUiState

/**
 * Step 1: Subunit name input with auto-focus.
 */
@Composable
fun SubunitNameStep(
    uiState: CreateEditSubunitUiState,
    onEvent: (CreateEditSubunitUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val nameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { nameFocusRequester.requestFocus() }

    WizardStepLayout(modifier = modifier) {
        SectionCard {
            StyledOutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(CreateEditSubunitUiEvent.UpdateName(it)) },
                label = stringResource(R.string.subunit_field_name),
                placeholder = stringResource(R.string.subunit_field_name_hint),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.asString(),
                capitalization = KeyboardCapitalization.Words,
                focusRequester = nameFocusRequester,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
