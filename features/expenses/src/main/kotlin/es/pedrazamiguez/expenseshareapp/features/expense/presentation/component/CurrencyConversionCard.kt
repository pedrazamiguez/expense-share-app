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
 * Visual sizing variant for [CurrencyConversionCard].
 *
 * [STANDARD] is used at the expense level (larger padding, prominent title).
 * [COMPACT] is used at the add-on level (tighter spacing, smaller title).
 */
enum class CardStyle {
    STANDARD,
    COMPACT
}

/**
 * Reusable currency-conversion card that displays an exchange rate input,
 * a calculated group-amount input, an optional loading indicator, and an
 * optional locked-rate hint.
 *
 * Both the expense-level [ExchangeRateSection] and the add-on-level exchange
 * rate section delegate to this component, passing their respective data and
 * using [CardStyle.STANDARD] or [CardStyle.COMPACT] for visual sizing.
 *
 * @param exchangeRateValue   Current exchange-rate text shown in the rate field.
 * @param exchangeRateLabel   Label for the rate field (e.g. "1 EUR = X THB").
 * @param groupAmountValue    Current group-amount text shown in the amount field.
 * @param groupAmountLabel    Label for the amount field (e.g. "Cost in EUR").
 * @param isLoadingRate       Whether a rate-fetch spinner should be shown.
 * @param isExchangeRateLocked  When true both fields become read-only.
 * @param onExchangeRateChanged Called when the user edits the rate field.
 * @param onGroupAmountChanged  Called when the user edits the group-amount field.
 * @param focusManager        Used to clear focus on "Done" keyboard action.
 * @param modifier            Outer modifier applied to the [Card].
 * @param cardStyle           Visual sizing variant — [CardStyle.STANDARD] (default)
 *                            or [CardStyle.COMPACT].
 * @param exchangeRateLockedHint Optional hint explaining why the rate is locked.
 * @param isInsufficientCash  Drives error colouring on the locked hint text.
 * @param isGroupAmountError  Shows error styling on the group-amount field.
 */
@Composable
fun CurrencyConversionCard(
    exchangeRateValue: String,
    exchangeRateLabel: String,
    groupAmountValue: String,
    groupAmountLabel: String,
    isLoadingRate: Boolean,
    isExchangeRateLocked: Boolean,
    onExchangeRateChanged: (String) -> Unit,
    onGroupAmountChanged: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    cardStyle: CardStyle = CardStyle.STANDARD,
    exchangeRateLockedHint: UiText? = null,
    isInsufficientCash: Boolean = false,
    isGroupAmountError: Boolean = false
) {
    val contentPadding: Dp
    val titleStyle: TextStyle
    val spinnerSize: Dp
    val titleToFieldSpacing: Dp

    when (cardStyle) {
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
            // ── Title Row ───────────────────────────────────────────
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

            Spacer(Modifier.height(titleToFieldSpacing))

            // ── Input Row ───────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledOutlinedTextField(
                    value = exchangeRateValue,
                    onValueChange = onExchangeRateChanged,
                    label = exchangeRateLabel,
                    modifier = Modifier.weight(1f),
                    readOnly = isExchangeRateLocked,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
                StyledOutlinedTextField(
                    value = groupAmountValue,
                    onValueChange = onGroupAmountChanged,
                    label = groupAmountLabel,
                    modifier = Modifier.weight(1f),
                    readOnly = isExchangeRateLocked,
                    keyboardType = KeyboardType.Decimal,
                    isError = isGroupAmountError,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }

            // ── Locked Hint ─────────────────────────────────────────
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
    }
}
