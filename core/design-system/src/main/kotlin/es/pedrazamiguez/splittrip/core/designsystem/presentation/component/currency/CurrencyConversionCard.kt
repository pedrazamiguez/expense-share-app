package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.extension.asString
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.AlertTriangle
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.rememberAutoFocusRequester
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard

private const val EXCHANGE_RATE_FIELD_WEIGHT = 0.6f
private const val GROUP_AMOUNT_FIELD_WEIGHT = 0.4f

/**
 * Reusable currency-conversion card that displays an exchange rate input,
 * a calculated group-amount input, an optional loading indicator, and an
 * optional locked-rate hint.
 *
 * Always uses the standard card style (20 dp content padding, titleSmall title).
 * The focus manager is resolved internally via [LocalFocusManager].
 *
 * @param state               Immutable display state for the card.
 * @param onExchangeRateChanged Called when the user edits the rate field.
 * @param onGroupAmountChanged  Called when the user edits the group-amount field.
 * @param modifier            Outer modifier applied to the [Surface].
 */
@Composable
fun CurrencyConversionCard(
    state: CurrencyConversionCardState,
    onExchangeRateChanged: (String) -> Unit,
    onGroupAmountChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = rememberAutoFocusRequester(state.autoFocus)

    FlatCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            ConversionCardTitleRow(
                title = state.title,
                isLoadingRate = state.isLoadingRate
            )
            Spacer(Modifier.height(12.dp))
            ConversionCardInputRow(
                state = state,
                onExchangeRateChanged = onExchangeRateChanged,
                onGroupAmountChanged = onGroupAmountChanged,
                onDone = { focusManager.clearFocus() },
                focusRequester = if (state.autoFocus) focusRequester else null,
                moveCursorToEndOnFocus = state.autoFocus
            )
            ConversionCardLockedHint(
                exchangeRateLockedHint = state.exchangeRateLockedHint,
                isInsufficientCash = state.isInsufficientCash
            )
            StaleRateBanner(isStale = state.isExchangeRateStale)
        }
    }
}

@Composable
private fun ConversionCardTitleRow(
    title: String,
    isLoadingRate: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isLoadingRate) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
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
    onDone: () -> Unit,
    focusRequester: FocusRequester? = null,
    moveCursorToEndOnFocus: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StyledOutlinedTextField(
            value = state.exchangeRateValue,
            onValueChange = onExchangeRateChanged,
            label = state.exchangeRateLabel,
            modifier = Modifier.weight(EXCHANGE_RATE_FIELD_WEIGHT),
            readOnly = state.isExchangeRateLocked,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            focusRequester = focusRequester,
            moveCursorToEndOnFocus = moveCursorToEndOnFocus
        )
        StyledOutlinedTextField(
            value = state.groupAmountValue,
            onValueChange = onGroupAmountChanged,
            label = state.groupAmountLabel,
            modifier = Modifier.weight(GROUP_AMOUNT_FIELD_WEIGHT),
            readOnly = state.isExchangeRateLocked,
            keyboardType = KeyboardType.Decimal,
            isError = state.isGroupAmountError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
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

@Composable
private fun StaleRateBanner(isStale: Boolean) {
    if (!isStale) return

    Spacer(Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = TablerIcons.Outline.AlertTriangle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(R.string.stale_rate_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
