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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.rememberAutoFocusRequester

/** Weight ratio for amount input field vs currency dropdown. */
private const val AMOUNT_FIELD_WEIGHT = 0.5f
private const val CURRENCY_FIELD_WEIGHT = 0.5f

/**
 * Reusable card combining an amount text field and a [CurrencyDropdown].
 *
 * Can be used for any "enter an amount + pick a currency" scenario — e.g.
 * withdrawal amounts, ATM fees, contribution amounts, etc.
 *
 * @param state             Combined display state (amount, currency, labels).
 * @param onAmountChanged   Called when the amount text changes.
 * @param onCurrencySelected Called with the currency code when a new currency is selected.
 * @param modifier          Outer modifier applied to the [Card].
 */
@Composable
fun AmountCurrencyCard(
    state: AmountCurrencyCardState,
    onAmountChanged: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = rememberAutoFocusRequester(state.autoFocus)

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
            if (state.title != null) {
                Text(
                    text = state.title,
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
                    value = state.amount,
                    onValueChange = onAmountChanged,
                    label = state.amountLabel,
                    modifier = Modifier.weight(AMOUNT_FIELD_WEIGHT),
                    keyboardType = KeyboardType.Decimal,
                    isError = state.isAmountError,
                    imeAction = ImeAction.Done,
                    focusRequester = if (state.autoFocus) focusRequester else null
                )
                CurrencyDropdown(
                    selectedCurrency = state.selectedCurrency,
                    availableCurrencies = state.availableCurrencies,
                    onCurrencySelected = onCurrencySelected,
                    label = state.currencyLabel,
                    modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
                )
            }
        }
    }
}
