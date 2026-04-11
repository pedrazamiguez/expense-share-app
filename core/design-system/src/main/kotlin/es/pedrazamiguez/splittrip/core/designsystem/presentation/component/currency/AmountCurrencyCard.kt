package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.rememberAutoFocusRequester
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Weight ratio for amount input field vs currency dropdown. */
private const val AMOUNT_FIELD_WEIGHT = 0.5f
private const val CURRENCY_FIELD_WEIGHT = 0.5f

/** Delay for re-focusing the amount field after the dropdown closes. */
private const val REFOCUS_DELAY_MS = 100L

/**
 * Reusable card combining an amount text field and a [CurrencyDropdown].
 *
 * Can be used for any "enter an amount + pick a currency" scenario — e.g.
 * withdrawal amounts, ATM fees, contribution amounts, etc.
 *
 * @param state             Combined display state (amount, currency, labels).
 * @param onAmountChanged   Called when the amount text changes.
 * @param onCurrencySelected Called with the currency code when a new currency is selected.
 * @param onImeAction       Optional callback invoked when the keyboard Done action fires.
 *                          When non-null, triggered after clearing focus.
 * @param modifier          Outer modifier applied to the [Surface].
 */
@Composable
fun AmountCurrencyCard(
    state: AmountCurrencyCardState,
    onAmountChanged: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onImeAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val focusRequester = rememberAutoFocusRequester(state.autoFocus)
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    FlatCard(modifier = modifier.fillMaxWidth()) {
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
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onImeAction?.invoke()
                        }
                    ),
                    focusRequester = if (state.autoFocus) focusRequester else null,
                    moveCursorToEndOnFocus = state.autoFocus
                )
                CurrencyDropdown(
                    selectedCurrency = state.selectedCurrency,
                    availableCurrencies = state.availableCurrencies,
                    onCurrencySelected = { code ->
                        onCurrencySelected(code)
                        if (state.autoFocus) {
                            coroutineScope.launch {
                                delay(REFOCUS_DELAY_MS)
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                    },
                    label = state.currencyLabel,
                    modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
                )
            }
        }
    }
}
