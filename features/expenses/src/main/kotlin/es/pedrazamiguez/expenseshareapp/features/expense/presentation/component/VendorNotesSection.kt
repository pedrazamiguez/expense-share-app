package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R

/**
 * Vendor and notes text input fields.
 */
@Composable
internal fun VendorNotesSection(
    vendor: String,
    notes: String,
    onVendorChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StyledOutlinedTextField(
            value = vendor,
            onValueChange = onVendorChanged,
            label = stringResource(R.string.add_expense_vendor_label),
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )

        StyledOutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            label = stringResource(R.string.add_expense_notes_label),
            capitalization = KeyboardCapitalization.Sentences,
            singleLine = false,
            maxLines = 3,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}
