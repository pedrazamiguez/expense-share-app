package es.pedrazamiguez.expenseshareapp.domain.converter

import es.pedrazamiguez.expenseshareapp.domain.exception.ValidationException
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

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
     *
     * Simple logic:
     * - Find the LAST separator (. or ,) → that's the decimal separator
     * - Remove all other separators before it
     * - Convert to US format (. as decimal) for parsing
     */
    fun parseToCents(amountString: String): Result<Long> {
        val cleanString = amountString.trim()
        if (cleanString.isBlank()) {
            return Result.failure(ValidationException("Amount cannot be empty"))
        }

        // Normalize to US format (. as decimal separator)
        val normalizedString = normalizeAmountString(cleanString)

        // Parse with US locale
        val usFormat = NumberFormat.getNumberInstance(Locale.US)
        usFormat.isGroupingUsed = false // We handle separators ourselves

        var amountDouble = parseStrict(normalizedString, usFormat)

        if (amountDouble == null) {
            return Result.failure(ValidationException("Please enter a valid amount"))
        }

        val amountInCents = BigDecimal
            .valueOf(amountDouble)
            .multiply(BigDecimal(100))
            .setScale(
                0,
                RoundingMode.HALF_UP
            )
            .toLong()

        if (amountInCents <= 0) {
            return Result.failure(ValidationException("Amount must be greater than zero"))
        }

        return Result.success(amountInCents)
    }

    /**
     * Normalizes amount string to US format (. = decimal) using simple logic:
     *
     * 1. Find the LAST separator (. or ,) → this is the decimal separator
     * 2. Remove all separators BEFORE it (they're thousand separators or user mistakes)
     * 3. Convert the last separator to dot (US decimal format)
     *
     * Examples:
     * - "1245.56" → last sep is . → remove nothing → "1245.56"
     * - "1.245,56" → last sep is , → remove . → "1245,56" → "1245.56"
     * - "1,245.56" → last sep is . → remove , → "1245.56"
     * - "6,666" → only one sep , → "6,666" → "6.666"
     * - "1.25" → only one sep . → "1.25"
     * - "" or blank → "0"
     */
    fun normalizeAmountString(input: String): String {
        if (input.isBlank()) return "0"
        val lastDotIndex = input.lastIndexOf('.')
        val lastCommaIndex = input.lastIndexOf(',')

        // No separators at all
        if (lastDotIndex == -1 && lastCommaIndex == -1) {
            return input
        }

        // Determine which separator is the decimal (the last one)
        val isLastSeparatorDot = lastDotIndex > lastCommaIndex
        val decimalSeparatorIndex = if (isLastSeparatorDot) lastDotIndex else lastCommaIndex

        // Build the normalized string
        val beforeDecimal = input.substring(0, decimalSeparatorIndex)
            .replace(".", "") // Remove all dots before decimal
            .replace(",", "") // Remove all commas before decimal

        val afterDecimal = input.substring(decimalSeparatorIndex + 1)

        // Return in US format (dot as decimal)
        return "$beforeDecimal.$afterDecimal"
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

