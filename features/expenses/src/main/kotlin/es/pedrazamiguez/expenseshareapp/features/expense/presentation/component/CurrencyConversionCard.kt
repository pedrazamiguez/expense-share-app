package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R

/**
 * Reusable currency-conversion card that displays an exchange rate input,
 * a calculated group-amount input, an optional loading indicator, and an
 * optional locked-rate hint.
 *
 * Both the expense-level [ExchangeRateSection] and the add-on-level exchange
 * rate section delegate to this component, passing their respective data via
 * [CurrencyConversionCardState] and using [CardStyle.STANDARD] or
 * [CardStyle.COMPACT] for visual sizing.
 *
 * @param state               Immutable display state for the card.
 * @param onExchangeRateChanged Called when the user edits the rate field.
 * @param onGroupAmountChanged  Called when the user edits the group-amount field.
 * @param focusManager        Used to clear focus on "Done" keyboard action.
 * @param modifier            Outer modifier applied to the [Card].
 */
@Composable
fun CurrencyConversionCard(
    state: CurrencyConversionCardState,
    onExchangeRateChanged: (String) -> Unit,
    onGroupAmountChanged: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    val contentPadding: Dp
    val titleStyle: TextStyle
    val spinnerSize: Dp
    val titleToFieldSpacing: Dp

    when (state.cardStyle) {
        CardStyle.STANDARD -> {
            contentPadding = 20.dp
            titleStyle = MaterialTheme.typography.titleSmall
            spinnerSize = 16.dp
            titleToFieldSpacing = 12.dp
        }
        CardStyle.COMPACT -> {
            contentPadding = 16.dp
            titleStyle = MaterialTheme.typography.labelMedium
            spinnerSize = 14.dp
            titleToFieldSpacing = 8.dp
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(contentPadding)) {
            ConversionCardTitleRow(
                isLoadingRate = state.isLoadingRate,
                titleStyle = titleStyle,
                spinnerSize = spinnerSize
            )
            Spacer(Modifier.height(titleToFieldSpacing))
            ConversionCardInputRow(
                state = state,
                onExchangeRateChanged = onExchangeRateChanged,
                onGroupAmountChanged = onGroupAmountChanged,
                focusManager = focusManager
            )
            ConversionCardLockedHint(
                exchangeRateLockedHint = state.exchangeRateLockedHint,
                isInsufficientCash = state.isInsufficientCash
            )
        }
    }
}

@Composable
private fun ConversionCardTitleRow(
    isLoadingRate: Boolean,
    titleStyle: TextStyle,
    spinnerSize: Dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.add_expense_exchange_rate_title),
            style = titleStyle,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isLoadingRate) {
            CircularProgressIndicator(
                modifier = Modifier.size(spinnerSize),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConversionCardInputRow(
    state: CurrencyConversionCardState,
    onExchangeRateChanged: (String) -> Unit,
    onGroupAmountChanged: (String) -> Unit,
    focusManager: FocusManager
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StyledOutlinedTextField(
            value = state.exchangeRateValue,
            onValueChange = onExchangeRateChanged,
            label = state.exchangeRateLabel,
            modifier = Modifier.weight(1f),
            readOnly = state.isExchangeRateLocked,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        )
        StyledOutlinedTextField(
            value = state.groupAmountValue,
            onValueChange = onGroupAmountChanged,
            label = state.groupAmountLabel,
            modifier = Modifier.weight(1f),
            readOnly = state.isExchangeRateLocked,
            keyboardType = KeyboardType.Decimal,
            isError = state.isGroupAmountError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}

@Composable
private fun ConversionCardLockedHint(
    exchangeRateLockedHint: UiText?,
    isInsufficientCash: Boolean
) {
    exchangeRateLockedHint?.let { hint ->
        Spacer(Modifier.height(8.dp))
        Text(
            text = hint.asString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (isInsufficientCash) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
