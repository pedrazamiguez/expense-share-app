package es.pedrazamiguez.expenseshareapp.domain.converter

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyConverter {

    fun convert(
        amount: BigDecimal, source: Currency, target: Currency, rates: List<ExchangeRate>
    ): BigDecimal {
        if (source.code == target.code) return amount

        val baseCurrency = rates.firstOrNull()?.baseCurrency
            ?: throw IllegalArgumentException("Rates list is empty")

        // 1. source → USD (base)
        val amountInBase = if (source.code == baseCurrency.code) {
            amount
        } else {
            val sourceRate = rates.find { it.currency.code == source.code }
                ?: throw IllegalArgumentException("Missing rate for ${source.code}")
            amount.divide(sourceRate.rate, 10, RoundingMode.HALF_UP)
        }

        // 2. base → target
        val result = if (target.code == baseCurrency.code) {
            amountInBase
        } else {
            val targetRate = rates.find { it.currency.code == target.code }
                ?: throw IllegalArgumentException("Missing rate for ${target.code}")
            amountInBase.multiply(targetRate.rate)
        }

        return result.setScale(target.decimalDigits, RoundingMode.HALF_UP)
    }
}
