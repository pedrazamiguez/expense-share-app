package es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
}
