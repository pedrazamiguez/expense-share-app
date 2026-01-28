package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for performing expense-related calculations.
 *
 * All monetary calculations should go through this service to ensure:
 * - Consistent precision handling
 * - Proper rounding rules
 * - Support for currencies with different decimal places (e.g., JPY has 0, TND has 3)
 */
class ExpenseCalculatorService {

    companion object {
        private const val RATE_PRECISION = 6
        private const val AMOUNT_PRECISION = 2
    }

    /**
     * Calculates the group amount from source amount and exchange rate.
     *
     * @param sourceAmount The amount in source currency
     * @param rate The exchange rate (source to target)
     * @return The calculated amount in group currency
     */
    fun calculateGroupAmount(sourceAmount: BigDecimal, rate: BigDecimal): BigDecimal {
        if (rate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        // Source * Rate = Target (e.g. 1000 THB * 0.027 = 27 EUR)
        return sourceAmount.multiply(rate).setScale(AMOUNT_PRECISION, RoundingMode.HALF_UP)
    }

    /**
     * Calculates the implied exchange rate from source and target amounts.
     *
     * @param sourceAmount The amount in source currency
     * @param groupAmount The amount in group currency
     * @return The implied exchange rate
     */
    fun calculateImpliedRate(sourceAmount: BigDecimal, groupAmount: BigDecimal): BigDecimal {
        if (sourceAmount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        // Target / Source = Rate (e.g. 27.35 EUR / 1000 THB = 0.02735)
        return groupAmount.divide(sourceAmount, RATE_PRECISION, RoundingMode.HALF_UP)
    }

    /**
     * Calculates the group amount from string inputs (UI layer convenience method).
     * Handles parsing and formatting, returning a formatted string result.
     *
     * @param sourceAmountString The source amount as entered by user
     * @param exchangeRateString The exchange rate as entered by user
     * @return Formatted string representation of the calculated group amount
     */
    fun calculateGroupAmountFromStrings(
        sourceAmountString: String,
        exchangeRateString: String
    ): String {
        val sourceCents = CurrencyConverter.parseToCents(sourceAmountString).getOrNull()
        val sourceAmount = sourceCents?.let { centsToBigDecimal(it) } ?: BigDecimal.ZERO
        val rate = exchangeRateString.toBigDecimalOrNull() ?: BigDecimal.ONE

        val result = calculateGroupAmount(sourceAmount, rate)
        return result.toPlainString()
    }

    /**
     * Calculates the implied exchange rate from string inputs (UI layer convenience method).
     * Handles parsing and formatting, returning a formatted string result.
     *
     * @param sourceAmountString The source amount as entered by user
     * @param groupAmountString The target group amount as entered by user
     * @return Formatted string representation of the implied exchange rate
     */
    fun calculateImpliedRateFromStrings(
        sourceAmountString: String,
        groupAmountString: String
    ): String {
        val sourceCents = CurrencyConverter.parseToCents(sourceAmountString).getOrNull()
        val sourceAmount = sourceCents?.let { centsToBigDecimal(it) } ?: BigDecimal.ZERO
        val targetAmount = groupAmountString.toBigDecimalOrNull() ?: BigDecimal.ZERO

        val result = calculateImpliedRate(sourceAmount, targetAmount)
        return result.stripTrailingZeros().toPlainString()
    }

    /**
     * Converts cents to BigDecimal amount.
     * Centralizes the conversion logic to handle currencies with different decimal places.
     *
     * @param cents The amount in smallest currency unit
     * @param decimalPlaces Number of decimal places for the currency (default 2)
     * @return BigDecimal representation of the amount
     */
    fun centsToBigDecimal(cents: Long, decimalPlaces: Int = 2): BigDecimal {
        val divisor = BigDecimal.TEN.pow(decimalPlaces)
        return BigDecimal(cents).divide(divisor, decimalPlaces, RoundingMode.HALF_UP)
    }
}
