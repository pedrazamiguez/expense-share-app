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

// ── Standard (expense-level) ────────────────────────────────────────────────

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "37.037",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "12.15",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = false,
            isExchangeRateLocked = false,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.STANDARD
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardLoadingPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = true,
            isExchangeRateLocked = false,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.STANDARD
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardLockedPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "36.50",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "12.33",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = false,
            isExchangeRateLocked = true,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.STANDARD,
            exchangeRateLockedHint = UiText.StringResource(
                R.string.add_expense_exchange_rate_title
            ),
            isInsufficientCash = false
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardErrorPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "37.037",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = false,
            isExchangeRateLocked = false,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.STANDARD,
            isGroupAmountError = true
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardStandardInsufficientCashPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "36.50",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "12.33",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = false,
            isExchangeRateLocked = true,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.STANDARD,
            exchangeRateLockedHint = UiText.StringResource(
                R.string.add_expense_exchange_rate_title
            ),
            isInsufficientCash = true
        )
    }
}

// ── Compact (add-on-level) ──────────────────────────────────────────────────

@PreviewComplete
@Composable
private fun CurrencyConversionCardCompactPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "37.037",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "2.70",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = false,
            isExchangeRateLocked = false,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.COMPACT
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardCompactLoadingPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = true,
            isExchangeRateLocked = false,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.COMPACT
        )
    }
}

@PreviewComplete
@Composable
private fun CurrencyConversionCardCompactLockedPreview() {
    PreviewThemeWrapper {
        CurrencyConversionCard(
            exchangeRateValue = "36.50",
            exchangeRateLabel = "1 EUR = X THB",
            groupAmountValue = "2.74",
            groupAmountLabel = "Cost in EUR",
            isLoadingRate = false,
            isExchangeRateLocked = true,
            onExchangeRateChanged = {},
            onGroupAmountChanged = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(16.dp),
            cardStyle = CardStyle.COMPACT,
            exchangeRateLockedHint = UiText.StringResource(
                R.string.add_expense_exchange_rate_title
            ),
            isInsufficientCash = false
        )
    }
}
