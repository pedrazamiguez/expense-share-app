package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun Expense.formatAmount(locale: Locale = Locale.getDefault()): String =
    formatCurrencyAmount(amount = groupAmount, currencyCode = groupCurrency, locale = locale)

fun Expense.formatSourceAmount(locale: Locale = Locale.getDefault()): String =
    formatCurrencyAmount(amount = sourceAmount, currencyCode = sourceCurrency, locale = locale)

fun formatCurrencyAmount(amount: Long, currencyCode: String, locale: Locale): String {
    val currencyInstance =
        runCatching { Currency.getInstance(currencyCode) }.getOrElse { Currency.getInstance(
            AppConstants.DEFAULT_CURRENCY_CODE) }
    val fractionDigits = currencyInstance.defaultFractionDigits
    val divisor = BigDecimal.TEN.pow(fractionDigits)
    val value = BigDecimal(amount).divide(divisor, fractionDigits, RoundingMode.HALF_UP)

    val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currencyInstance
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }

    val formatted = numberFormat.format(value)

    // NumberFormat may render the ISO code (e.g. "CNY") instead of the native symbol
    // (e.g. "¥") when the user locale doesn't recognise the currency.
    // Fix: resolve the symbol via the currency's own locale and substitute it.
    val localeSymbol = currencyInstance.getSymbol(locale)
    val nativeSymbol = resolveNativeSymbol(currencyInstance)

    val finalFormatted = if (nativeSymbol != null && localeSymbol != nativeSymbol && localeSymbol == currencyInstance.currencyCode) {
        formatted.replace(localeSymbol, nativeSymbol)
    } else {
        formatted
    }

    // Replace standard spaces with non-breaking spaces (\u00A0)
    // to prevent the currency symbol from detaching on line breaks
    return finalFormatted.replace(" ", "\u00A0")
}

/**
 * Finds the symbol for a [Currency] by looking up a locale whose country
 * actually uses that currency (its "native" locale). Returns `null` when no
 * matching locale is found or the symbol is still the ISO code.
 */
private fun resolveNativeSymbol(currency: Currency): String? {
    val nativeLocale = Locale.getAvailableLocales().firstOrNull { locale ->
        locale.country.isNotEmpty() && runCatching {
            Currency.getInstance(locale) == currency
        }.getOrDefault(false)
    } ?: return null

    var symbol = currency.getSymbol(nativeLocale)

    // Disambiguate common shared symbols to avoid UI confusion
    if (symbol == "$") {
        symbol = when (currency.currencyCode) {
            "USD" -> "US$" // Standardises USD
            "MXN" -> "MX$" // Distinct symbol for Mexican Pesos
            "CAD" -> "CA$" // Canadian Dollars
            "AUD" -> "AU$" // Australian Dollars
            "COP" -> "CO$" // Colombian Pesos
            "CLP" -> "CL$" // Chilean Pesos
            "ARS" -> "AR$" // Argentine Pesos
            // Fallback: prepend the first two letters of the ISO code
            else -> "${currency.currencyCode.take(2)}$"
        }
    }

    return symbol.takeIf { it != currency.currencyCode }
}

fun es.pedrazamiguez.expenseshareapp.domain.model.Currency.formatDisplay(): String {
    val currencyInstance =
        runCatching { Currency.getInstance(code) }.getOrElse { Currency.getInstance(
            AppConstants.DEFAULT_CURRENCY_CODE) }
    val nativeSymbol = resolveNativeSymbol(currencyInstance)

    return if (nativeSymbol?.isNotBlank() == true && nativeSymbol != code) {
        "$code ($nativeSymbol)"
    } else {
        code
    }
}
