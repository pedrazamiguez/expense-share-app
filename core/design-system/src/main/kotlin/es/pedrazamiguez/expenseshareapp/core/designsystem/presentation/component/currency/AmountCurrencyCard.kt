package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import kotlinx.collections.immutable.ImmutableList

/** Weight ratio for amount input field vs currency dropdown. */
private const val AMOUNT_FIELD_WEIGHT = 0.55f
private const val CURRENCY_FIELD_WEIGHT = 0.45f

/**
 * Reusable card combining an amount text field and a [CurrencyDropdown].
 *
 * Can be used for any "enter an amount + pick a currency" scenario — e.g.
 * withdrawal amounts, ATM fees, contribution amounts, etc.
 *
 * @param amount              Current amount text.
 * @param isAmountError       Whether the amount field should show error styling.
 * @param selectedCurrency    Currently selected [CurrencyUiModel].
 * @param availableCurrencies All available currencies for the dropdown.
 * @param onAmountChanged     Called when the amount text changes.
 * @param onCurrencySelected  Called with the currency code when a new currency is selected.
 * @param amountLabel         Localised hint label for the amount field.
 * @param currencyLabel       Localised hint label for the currency dropdown.
 * @param title               Optional card title shown above the fields.
 * @param autoFocus           If `true`, the amount field requests focus on first composition.
 * @param modifier            Outer modifier applied to the [Card].
 */
@Composable
fun AmountCurrencyCard(
    amount: String,
    isAmountError: Boolean,
    selectedCurrency: CurrencyUiModel?,
    availableCurrencies: ImmutableList<CurrencyUiModel>,
    onAmountChanged: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    amountLabel: String,
    currencyLabel: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    autoFocus: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledOutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    label = amountLabel,
                    modifier = Modifier.weight(AMOUNT_FIELD_WEIGHT),
                    keyboardType = KeyboardType.Decimal,
                    isError = isAmountError,
                    imeAction = ImeAction.Done,
                    focusRequester = if (autoFocus) focusRequester else null
                )
                CurrencyDropdown(
                    selectedCurrency = selectedCurrency,
                    availableCurrencies = availableCurrencies,
                    onCurrencySelected = onCurrencySelected,
                    label = currencyLabel,
                    modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
                )
            }
        }
    }
}

