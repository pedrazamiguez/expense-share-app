package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun Expense.formatAmount(locale: Locale = Locale.getDefault()): String {
    val fractionDigits = currency.defaultFractionDigits
    val divisor = BigDecimal.TEN.pow(fractionDigits)
    val amount = BigDecimal(amountCents)
        .divide(divisor, fractionDigits, RoundingMode.HALF_UP)

    val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = this@formatAmount.currency
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }

    return numberFormat.format(amount)
}
