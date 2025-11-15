package es.pedrazamiguez.expenseshareapp.domain.converter

import es.pedrazamiguez.expenseshareapp.domain.exception.ValidationException
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyConverter {

    fun convert(
        amount: BigDecimal, source: Currency, target: Currency, rates: ExchangeRates
    ): BigDecimal {

        if (source.code == target.code) return amount

        val baseCurrency = rates.baseCurrency

        // 1. source → base
        val amountInBase = if (source.code == baseCurrency.code) {
            amount
        } else {
            val sourceRate = rates.exchangeRates.find { it.currency.code == source.code }
                ?: throw IllegalArgumentException("Missing rate for ${source.code}")
            amount.divide(
                sourceRate.rate,
                10,
                RoundingMode.HALF_UP
            )
        }

        // 2. base → target
        val result = if (target.code == baseCurrency.code) {
            amountInBase
        } else {
            val targetRate = rates.exchangeRates.find { it.currency.code == target.code }
                ?: throw IllegalArgumentException("Missing rate for ${target.code}")
            amountInBase.multiply(targetRate.rate)
        }

        return result.setScale(
            target.decimalDigits,
            RoundingMode.HALF_UP
        )
    }

    /**
     * Parses a raw amount string into cents.
     * Returns a Result type to clearly communicate all failure states.
     */
    fun parseToCents(amountString: String): Result<Long> {
        val cleanString = amountString.trim()
        if (cleanString.isBlank()) {
            return Result.failure(ValidationException("Amount cannot be empty"))
        }

        // 1. Try strict parsing with default locale
        val localeFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        localeFormat.isGroupingUsed = false
        var amountDouble = parseStrict(
            cleanString,
            localeFormat
        )

        if (amountDouble == null) {
            // 2. If locale parse failed, try the *other* main separator
            val decimalSep = (localeFormat as? java.text.DecimalFormat)?.decimalFormatSymbols?.decimalSeparator ?: '.'
            val fallbackFormat: NumberFormat? = when (decimalSep) {
                '.' -> NumberFormat.getNumberInstance(Locale.GERMAN) // Try ','
                ',' -> NumberFormat.getNumberInstance(Locale.US)     // Try '.'
                else -> null
            }

            if (fallbackFormat != null) {
                fallbackFormat.isGroupingUsed = false
                amountDouble = parseStrict(
                    cleanString,
                    fallbackFormat
                )
            }
        }

        // 3. Check for invalid format or non-positive numbers
        if (amountDouble == null) {
            return Result.failure(ValidationException("Please enter a valid amount"))
        }

        val amountInCents = BigDecimal.valueOf(amountDouble).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toLong()

        if (amountInCents <= 0) {
            return Result.failure(ValidationException("Amount must be greater than zero"))
        }

        return Result.success(amountInCents)
    }

    private fun parseStrict(str: String, format: NumberFormat): Double? {
        val pos = ParsePosition(0)
        val parsed = format.parse(
            str,
            pos
        )
        return if (pos.index == str.length && pos.errorIndex == -1) {
            parsed?.toDouble()
        } else {
            null
        }
    }

}

