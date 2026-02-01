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
        private const val DEFAULT_DECIMAL_PLACES = 2
    }

    /**
     * Calculates the group amount from source amount and exchange rate.
     *
     * @param sourceAmount The amount in source currency
     * @param rate The exchange rate (source to target)
     * @param targetDecimalPlaces Number of decimal places for the target currency (default 2)
     * @return The calculated amount in group currency
     */
    fun calculateGroupAmount(
        sourceAmount: BigDecimal,
        rate: BigDecimal,
        targetDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): BigDecimal {
        if (rate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        // Source * Rate = Target (e.g. 1000 THB * 0.027 = 27 EUR)
        return sourceAmount.multiply(rate).setScale(targetDecimalPlaces, RoundingMode.HALF_UP)
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
     * @param exchangeRateString The exchange rate as entered by user (source to group format)
     * @param sourceDecimalPlaces Number of decimal places for the source currency (default 2)
     * @param targetDecimalPlaces Number of decimal places for the target currency (default 2)
     * @return Formatted string representation of the calculated group amount
     */
    fun calculateGroupAmountFromStrings(
        sourceAmountString: String,
        exchangeRateString: String,
        sourceDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        targetDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): String {
        val sourceAmount = parseAmount(sourceAmountString, sourceDecimalPlaces)
        val rate = exchangeRateString.toBigDecimalOrNull() ?: BigDecimal.ONE

        val result = calculateGroupAmount(sourceAmount, rate, targetDecimalPlaces)
        return result.toPlainString()
    }

    /**
     * Calculates the group amount from source amount using a user-friendly display rate.
     *
     * The display rate is in "group to source" format (e.g., "1 EUR = 37 THB"),
     * which is the inverse of the internal calculation rate.
     *
     * @param sourceAmountString The source amount as entered by user
     * @param displayRateString The display exchange rate (1 GroupCurrency = X SourceCurrency)
     * @param sourceDecimalPlaces Number of decimal places for the source currency (default 2)
     * @param targetDecimalPlaces Number of decimal places for the target currency (default 2)
     * @return Formatted string representation of the calculated group amount
     */
    fun calculateGroupAmountFromDisplayRate(
        sourceAmountString: String,
        displayRateString: String,
        sourceDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        targetDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): String {
        val sourceAmount = parseAmount(sourceAmountString, sourceDecimalPlaces)
        val displayRate = displayRateString.toBigDecimalOrNull() ?: BigDecimal.ONE

        if (displayRate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO.toPlainString()

        // Convert display rate (group to source) to calculation rate (source to group)
        // If 1 EUR = 37 THB, then 1 THB = 1/37 EUR
        val calculationRate = BigDecimal.ONE.divide(displayRate, RATE_PRECISION, RoundingMode.HALF_UP)

        val result = calculateGroupAmount(sourceAmount, calculationRate, targetDecimalPlaces)
        return result.toPlainString()
    }

    /**
     * Calculates the implied display exchange rate from source and group amounts.
     *
     * Returns the rate in user-friendly "group to source" format (e.g., "1 EUR = 37 THB"),
     * which is the inverse of the internal calculation rate.
     *
     * @param sourceAmountString The source amount as entered by user
     * @param groupAmountString The target group amount as entered by user
     * @param sourceDecimalPlaces Number of decimal places for the source currency (default 2)
     * @return Formatted string representation of the implied display exchange rate
     */
    fun calculateImpliedDisplayRateFromStrings(
        sourceAmountString: String,
        groupAmountString: String,
        sourceDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): String {
        val sourceAmount = parseAmount(sourceAmountString, sourceDecimalPlaces)
        val targetAmount = groupAmountString.toBigDecimalOrNull() ?: BigDecimal.ZERO

        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO.toPlainString()

        // Display rate = source / target (e.g., 1000 THB / 27 EUR = 37 THB per EUR)
        val displayRate = sourceAmount.divide(targetAmount, RATE_PRECISION, RoundingMode.HALF_UP)
        return displayRate.stripTrailingZeros().toPlainString()
    }

    /**
     * Converts a display exchange rate to the internal calculation rate.
     *
     * @param displayRateString The display rate in "group to source" format
     * @return The internal rate in "source to group" format (1/displayRate)
     */
    fun displayRateToCalculationRate(displayRateString: String): BigDecimal {
        val displayRate = displayRateString.toBigDecimalOrNull() ?: BigDecimal.ONE
        if (displayRate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return BigDecimal.ONE.divide(displayRate, RATE_PRECISION, RoundingMode.HALF_UP)
    }

    /**
     * Calculates the implied exchange rate from string inputs (UI layer convenience method).
     * Handles parsing and formatting, returning a formatted string result.
     *
     * @param sourceAmountString The source amount as entered by user
     * @param groupAmountString The target group amount as entered by user
     * @param sourceDecimalPlaces Number of decimal places for the source currency (default 2)
     * @return Formatted string representation of the implied exchange rate (source to group format)
     */
    fun calculateImpliedRateFromStrings(
        sourceAmountString: String,
        groupAmountString: String,
        sourceDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): String {
        val sourceAmount = parseAmount(sourceAmountString, sourceDecimalPlaces)
        val targetAmount = groupAmountString.toBigDecimalOrNull() ?: BigDecimal.ZERO

        val result = calculateImpliedRate(sourceAmount, targetAmount)
        return result.stripTrailingZeros().toPlainString()
    }

    /**
     * Parses an amount string to BigDecimal with proper scale for the currency.
     * Handles different locale formats (e.g., "1.234,56" vs "1,234.56") by normalizing
     * the decimal separator.
     *
     * @param amountString The amount as entered by user
     * @param decimalPlaces Number of decimal places for the currency
     * @return BigDecimal representation of the amount, or ZERO if parsing fails
     */
    private fun parseAmount(amountString: String, decimalPlaces: Int): BigDecimal {
        val cleanString = amountString.trim()
        if (cleanString.isBlank()) return BigDecimal.ZERO

        // Normalize to standard format with dot as decimal separator
        val normalizedString = CurrencyConverter.normalizeAmountString(cleanString)

        return normalizedString.toBigDecimalOrNull()
            ?.setScale(decimalPlaces, RoundingMode.HALF_UP)
            ?: BigDecimal.ZERO
    }


    /**
     * Converts cents to BigDecimal amount.
     * Centralizes the conversion logic to handle currencies with different decimal places.
     *
     * @param cents The amount in smallest currency unit
     * @param decimalPlaces Number of decimal places for the currency (default 2)
     * @return BigDecimal representation of the amount
     */
    fun centsToBigDecimal(cents: Long, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): BigDecimal {
        val divisor = BigDecimal.TEN.pow(decimalPlaces)
        return BigDecimal(cents).divide(divisor, decimalPlaces, RoundingMode.HALF_UP)
    }
}
