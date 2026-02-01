package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val DEFAULT_RATE_DECIMALS = 6

/**
 * Formats a number string from internal format (dot decimal) to locale-aware display format.
 *
 * @param locale The locale to use for formatting (default: device locale)
 * @param maxDecimalPlaces Maximum decimal places to show
 * @return The formatted string in locale format (e.g., "37,22" for Spanish)
 */
fun String.formatNumberForDisplay(
    locale: Locale = Locale.getDefault(),
    maxDecimalPlaces: Int = DEFAULT_RATE_DECIMALS
): String {
    val number = this.toBigDecimalOrNull() ?: return this

    val symbols = DecimalFormatSymbols.getInstance(locale)
    val pattern = buildNumberPattern(maxDecimalPlaces)
    val formatter = DecimalFormat(pattern, symbols)

    return formatter.format(number)
}

/**
 * Formats an exchange rate for display using locale-aware formatting.
 *
 * @param locale The locale to use for formatting (default: device locale)
 * @return Locale-formatted string (e.g., "37,22" for Spanish)
 */
fun String.formatRateForDisplay(locale: Locale = Locale.getDefault()): String {
    return this.formatNumberForDisplay(locale, DEFAULT_RATE_DECIMALS)
}

/**
 * Formats a BigDecimal number to locale-aware display format.
 *
 * @param locale The locale to use for formatting (default: device locale)
 * @param maxDecimalPlaces Maximum decimal places to show
 * @return The formatted string in locale format
 */
fun BigDecimal.formatForDisplay(
    locale: Locale = Locale.getDefault(),
    maxDecimalPlaces: Int = 2
): String {
    val symbols = DecimalFormatSymbols.getInstance(locale)
    val pattern = buildNumberPattern(maxDecimalPlaces)
    val formatter = DecimalFormat(pattern, symbols)

    return formatter.format(this)
}

private fun buildNumberPattern(maxDecimalPlaces: Int): String {
    return if (maxDecimalPlaces > 0) {
        "#,##0." + "#".repeat(maxDecimalPlaces)
    } else {
        "#,##0"
    }
}
