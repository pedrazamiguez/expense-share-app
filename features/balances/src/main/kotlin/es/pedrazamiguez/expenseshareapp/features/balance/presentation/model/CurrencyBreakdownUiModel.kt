package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single currency's amount with its group-currency equivalent.
 * Reused across cash-in-hand, cash-spent, and non-cash-spent per-currency breakdowns.
 * Pre-formatted by the mapper for direct display.
 *
 * @param currency ISO 4217 currency code (e.g., "THB").
 * @param formattedAmount Native currency formatted amount (e.g., "477.50 ฿").
 * @param formattedEquivalent Group currency equivalent (e.g., "12.82 €").
 *                            Empty string when [currency] is the group currency
 *                            or when the equivalent amount is zero or negative.
 */
data class CurrencyBreakdownUiModel(
    val currency: String = "",
    val formattedAmount: String = "",
    val formattedEquivalent: String = ""
)

