package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import kotlinx.collections.immutable.ImmutableList

/**
 * Reusable currency selector dropdown backed by [StyledOutlinedTextField] in read-only mode.
 *
 * Shows the currently selected currency's [CurrencyUiModel.displayText] and opens a
 * dropdown with all [availableCurrencies] on tap.  Emits the selected ISO 4217 code
 * via [onCurrencySelected].
 *
 * @param selectedCurrency    Currently selected currency (may be `null` when nothing is chosen yet).
 * @param availableCurrencies Full list of selectable currencies.
 * @param onCurrencySelected  Called with the ISO 4217 [CurrencyUiModel.code] of the picked currency.
 * @param label               Localised hint label for the text field.
 * @param isLoading           When `true`, shows a loading spinner instead of the arrow icon and
 *                            prevents the dropdown from opening. Defaults to `false`.
 * @param modifier            Outer modifier applied to the wrapping [Box].
 */
@Composable
fun CurrencyDropdown(
    selectedCurrency: CurrencyUiModel?,
    availableCurrencies: ImmutableList<CurrencyUiModel>,
    onCurrencySelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Box(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        StyledOutlinedTextField(
            value = selectedCurrency?.displayText ?: "",
            onValueChange = {},
            readOnly = true,
            focusable = false,
            label = label,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            },
            onClick = { if (!isLoading) expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            // Non-focusable popup: the popup window does not take input focus, so the soft
            // keyboard remains visible while the user selects a currency.
            properties = PopupProperties(focusable = false)
        ) {
            availableCurrencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.displayText) },
                    onClick = {
                        onCurrencySelected(currency.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
