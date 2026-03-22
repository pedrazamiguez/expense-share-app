package es.pedrazamiguez.expenseshareapp.features.expense.presentation.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.CardStyle
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.CurrencyConversionCard
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.CurrencyConversionCardState

// ── Standard (expense-level) ────────────────────────────────────────────────

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "37.037",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "12.15",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = false,
                isExchangeRateLocked = false
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardLoadingPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = true,
                isExchangeRateLocked = false
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardLockedPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "36.50",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "12.33",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = false,
                isExchangeRateLocked = true,
                exchangeRateLockedHint = UiText.StringResource(
                    R.string.add_expense_cash_rate_locked_hint
                ),
                isInsufficientCash = false
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardErrorPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "37.037",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = false,
                isExchangeRateLocked = false,
                isGroupAmountError = true
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardInsufficientCashPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "36.50",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "12.33",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = false,
                isExchangeRateLocked = true,
                exchangeRateLockedHint = UiText.StringResource(
                    R.string.add_expense_cash_insufficient_hint
                ),
                isInsufficientCash = true
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// ── Compact (add-on-level) ──────────────────────────────────────────────────

@PreviewComplete
@Composable
private fun CurrencyConversionCardCompactPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "37.037",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "2.70",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = false,
                isExchangeRateLocked = false,
                cardStyle = CardStyle.COMPACT
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardCompactLoadingPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = true,
                isExchangeRateLocked = false,
                cardStyle = CardStyle.COMPACT
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardCompactLockedPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                exchangeRateValue = "36.50",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountValue = "2.74",
                groupAmountLabel = "Cost in EUR",
                isLoadingRate = false,
                isExchangeRateLocked = true,
                cardStyle = CardStyle.COMPACT,
                exchangeRateLockedHint = UiText.StringResource(
                    R.string.add_expense_cash_rate_locked_hint
                ),
                isInsufficientCash = false
            ),
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp)
        )
    }
}
