package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun Expense.formatAmount(locale: Locale = Locale.getDefault()): String {
    val currencyInstance =
        runCatching { Currency.getInstance(groupCurrency) }.getOrElse { Currency.getInstance("EUR") }
    val fractionDigits = currencyInstance.defaultFractionDigits
    val divisor = BigDecimal.TEN.pow(fractionDigits)
    val amount = BigDecimal(groupAmount)
        .divide(divisor, fractionDigits, RoundingMode.HALF_UP)

    val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currencyInstance
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }

    return numberFormat.format(amount)
}
