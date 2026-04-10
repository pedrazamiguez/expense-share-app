package es.pedrazamiguez.splittrip.core.designsystem.presentation.model

/**
 * Presentation model for a selectable currency option.
 *
 * Shared across features (expenses, balances) and consumed by common UI components
 * such as [es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency.CurrencyDropdown]
 * and [es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency.AmountCurrencyCard].
 *
 * @param code          ISO 4217 currency code (e.g. "EUR", "THB").
 * @param displayText   Human-readable label shown in the UI (e.g. "EUR (€)").
 * @param decimalDigits Number of decimal places for amounts in this currency.
 * @param defaultName   Full English name of the currency (e.g. "Euro"). Used as secondary display
 *                      text and for name-based search matching. Defaults to empty string.
 * @param localizedName Locale-aware name of the currency (e.g. "Libra esterlina" for GBP in Spanish).
 *                      Resolved via string resources for known currencies; falls back to [defaultName]
 *                      for currencies not in the domain enum. Used alongside [defaultName] for search
 *                      matching so users can find currencies in their own language. Defaults to empty string.
 */
data class CurrencyUiModel(
    val code: String,
    val displayText: String,
    val decimalDigits: Int,
    val defaultName: String = "",
    val localizedName: String = ""
)
