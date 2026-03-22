package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

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
 * Immutable state holder for [CurrencyConversionCard].
 *
 * Bundles all display-only fields so the composable signature stays concise
 * and callers can construct the state from their own model (e.g.,
 * `AddExpenseUiState` or `AddOnUiModel`).
 *
 * @param exchangeRateValue   Current exchange-rate text shown in the rate field.
 * @param exchangeRateLabel   Label for the rate field (e.g. "1 EUR = X THB").
 * @param groupAmountValue    Current group-amount text shown in the amount field.
 * @param groupAmountLabel    Label for the amount field (e.g. "Cost in EUR").
 * @param isLoadingRate       Whether a rate-fetch spinner should be shown.
 * @param isExchangeRateLocked When true both fields become read-only.
 * @param cardStyle           Visual sizing variant — [CardStyle.STANDARD] (default)
 *                            or [CardStyle.COMPACT].
 * @param exchangeRateLockedHint Optional hint explaining why the rate is locked.
 * @param isInsufficientCash  Drives error colouring on the locked hint text.
 * @param isGroupAmountError  Shows error styling on the group-amount field.
 */
data class CurrencyConversionCardState(
    val exchangeRateValue: String,
    val exchangeRateLabel: String,
    val groupAmountValue: String,
    val groupAmountLabel: String,
    val isLoadingRate: Boolean,
    val isExchangeRateLocked: Boolean,
    val cardStyle: CardStyle = CardStyle.STANDARD,
    val exchangeRateLockedHint: UiText? = null,
    val isInsufficientCash: Boolean = false,
    val isGroupAmountError: Boolean = false
)
