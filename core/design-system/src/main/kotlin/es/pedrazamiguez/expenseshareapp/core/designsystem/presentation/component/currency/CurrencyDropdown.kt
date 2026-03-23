package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
 * @param modifier            Outer modifier applied to the wrapping [Box].
 */
@Composable
fun CurrencyDropdown(
    selectedCurrency: CurrencyUiModel?,
    availableCurrencies: ImmutableList<CurrencyUiModel>,
    onCurrencySelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        StyledOutlinedTextField(
            value = selectedCurrency?.displayText ?: "",
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

