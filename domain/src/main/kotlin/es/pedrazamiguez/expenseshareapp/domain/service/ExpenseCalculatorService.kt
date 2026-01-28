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
     * @param exchangeRateString The exchange rate as entered by user
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
     * Calculates the implied exchange rate from string inputs (UI layer convenience method).
     * Handles parsing and formatting, returning a formatted string result.
     *
     * @param sourceAmountString The source amount as entered by user
     * @param groupAmountString The target group amount as entered by user
     * @param sourceDecimalPlaces Number of decimal places for the source currency (default 2)
     * @return Formatted string representation of the implied exchange rate
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
     * Uses CurrencyConverter for normalization but converts to BigDecimal with correct decimal places.
     *
     * @param amountString The amount as entered by user
     * @param decimalPlaces Number of decimal places for the currency
     * @return BigDecimal representation of the amount, or ZERO if parsing fails
     */
    private fun parseAmount(amountString: String, decimalPlaces: Int): BigDecimal {
        // Use CurrencyConverter to parse to cents (always multiplies by 100)
        val centsResult = CurrencyConverter.parseToCents(amountString).getOrNull()
            ?: return BigDecimal.ZERO

        // CurrencyConverter.parseToCents assumes 2 decimal places (multiplies by 100).
        // First convert back to a decimal value (divide by 100), then interpret with correct scale.
        // The parsed "cents" value represents the input × 100, regardless of actual currency.
        // For a 2-decimal currency: "123.45" → 12345 cents → 123.45 (correct)
        // For a 0-decimal currency: "15725" → 1572500 cents → need to divide by 100 → 15725 (correct)
        // For a 3-decimal currency: "12.345" → 1234.5 → rounds to 1235 cents → 12.35 (loses precision!)
        // Therefore, we convert cents back to the base amount and set proper scale.
        return BigDecimal(centsResult)
            .divide(BigDecimal(100), decimalPlaces, RoundingMode.HALF_UP)
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
