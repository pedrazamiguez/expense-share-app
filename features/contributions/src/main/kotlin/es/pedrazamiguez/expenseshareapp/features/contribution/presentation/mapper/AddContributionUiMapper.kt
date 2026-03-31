package es.pedrazamiguez.expenseshareapp.features.contribution.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmountWithCurrency
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.resolveCurrencySymbol

/**
 * Presentation-layer mapper for the Add Contribution wizard.
 *
 * Provides locale-aware formatting utilities consumed by [AddContributionViewModel].
 * Follows the **concrete-only** UiMapper pattern (no interface).
 */
class AddContributionUiMapper(
    private val localeProvider: LocaleProvider
) {

    /**
     * Formats a raw user-entered amount string with currency symbol and locale formatting.
     *
     * Delegates to the design-system [formatAmountWithCurrency] utility.
     */
    fun formatInputAmountWithCurrency(amountInput: String, currencyCode: String): String =
        formatAmountWithCurrency(amountInput, currencyCode, localeProvider.getCurrentLocale())

    /**
     * Resolves the currency symbol for a given ISO 4217 currency code.
     *
     * Delegates to the design-system [resolveCurrencySymbol] utility which
     * falls back to the currency's native locale when the user's locale
     * returns the ISO code (e.g. "INR" instead of "₹").
     *
     * @return The human-readable symbol (e.g. "€", "₹", "US$"), or an empty
     *         string if the code is blank or unresolvable.
     */
    fun resolveCurrencySymbol(currencyCode: String): String =
        resolveCurrencySymbol(currencyCode, localeProvider.getCurrentLocale())
}
