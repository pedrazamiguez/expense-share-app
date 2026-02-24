package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

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

private fun formatCurrencyAmount(amount: Long, currencyCode: String, locale: Locale): String {
    val currencyInstance =
        runCatching { Currency.getInstance(currencyCode) }.getOrElse { Currency.getInstance("EUR") }
    val fractionDigits = currencyInstance.defaultFractionDigits
    val divisor = BigDecimal.TEN.pow(fractionDigits)
    val value = BigDecimal(amount).divide(divisor, fractionDigits, RoundingMode.HALF_UP)

    val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currencyInstance
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }

    return numberFormat.format(value)
}
