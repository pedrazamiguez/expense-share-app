package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
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
        val rate = parseRate(exchangeRateString)

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
        val displayRate = parseRate(displayRateString)

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
        val targetAmount = parseAmountOrZero(groupAmountString) // Use amount parsing (returns ZERO for invalid)

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
        val displayRate = parseRate(displayRateString)
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
        val targetAmount = parseAmountOrZero(groupAmountString) // Use amount parsing (returns ZERO for invalid)

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
     * Parses a rate string to BigDecimal.
     * Handles different locale formats (e.g., "37,22" vs "37.22") by normalizing
     * the decimal separator using CurrencyConverter.
     *
     * @param rateString The rate as entered by user (may use comma or dot as decimal separator)
     * @return BigDecimal representation of the rate, or ONE if parsing fails (to avoid division by zero)
     */
    private fun parseRate(rateString: String): BigDecimal {
        val cleanString = rateString.trim()
        if (cleanString.isBlank()) return BigDecimal.ONE

        // Normalize to standard format with dot as decimal separator
        val normalizedString = CurrencyConverter.normalizeAmountString(cleanString)

        return normalizedString.toBigDecimalOrNull() ?: BigDecimal.ONE
    }

    /**
     * Parses an amount string to BigDecimal without scale adjustment.
     * Handles different locale formats (e.g., "27,03" vs "27.03") by normalizing
     * the decimal separator using CurrencyConverter.
     *
     * @param amountString The amount as entered by user (may use comma or dot as decimal separator)
     * @return BigDecimal representation of the amount, or ZERO if parsing fails
     */
    private fun parseAmountOrZero(amountString: String): BigDecimal {
        val cleanString = amountString.trim()
        if (cleanString.isBlank()) return BigDecimal.ZERO

        // Normalize to standard format with dot as decimal separator
        val normalizedString = CurrencyConverter.normalizeAmountString(cleanString)

        return normalizedString.toBigDecimalOrNull() ?: BigDecimal.ZERO
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

    // ── Add-On Calculations ──────────────────────────────────────────────

    /**
     * Sums the [AddOn.groupAmountCents] of all ON_TOP add-ons that are NOT discounts.
     *
     * INCLUDED add-ons are informational (they don't change the effective total).
     * Discounts are handled separately in [calculateEffectiveGroupAmount].
     *
     * @param addOns The list of add-ons attached to an expense.
     * @return The total group-currency amount of on-top, non-discount add-ons.
     */
    fun calculateTotalOnTopAddOns(addOns: List<AddOn>): Long =
        addOns.filter { it.mode == AddOnMode.ON_TOP && it.type != AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

    /**
     * Computes the effective group debt for an expense, accounting for add-ons.
     *
     * Formula: `baseGroupAmount + ON_TOP (non-discount) − DISCOUNT`
     *
     * - **ON_TOP** add-ons (fees, tips, surcharges) increase the total.
     * - **DISCOUNT** add-ons reduce the total.
     * - **INCLUDED** add-ons are purely informational and do NOT alter the total.
     *
     * When [addOns] is empty the result equals [baseGroupAmount] — no behavioral
     * change for existing expenses.
     *
     * @param baseGroupAmount The expense's raw `groupAmount` (in minor units).
     * @param addOns The structured add-ons list.
     * @return The effective group amount in minor units.
     */
    fun calculateEffectiveGroupAmount(baseGroupAmount: Long, addOns: List<AddOn>): Long {
        if (addOns.isEmpty()) return baseGroupAmount

        val onTop = addOns
            .filter { it.mode == AddOnMode.ON_TOP && it.type != AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

        val discounts = addOns
            .filter { it.type == AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

        return (baseGroupAmount + onTop - discounts).coerceAtLeast(0L)
    }

    /**
     * Computes the base cost of an expense after extracting INCLUDED add-ons.
     *
     * INCLUDED add-ons are portions already contained within the total. The base cost
     * is the remaining amount once those portions are removed:
     *
     * 1. **EXACT** INCLUDED add-ons are subtracted directly:
     *    `afterExact = total − sumOfExactIncludedCents`
     *
     * 2. **PERCENTAGE** INCLUDED add-ons are extracted via division:
     *    `baseCost = afterExact / (1 + sumOfPercentages / 100)`
     *
     * @param totalAmountCents       The total expense amount in minor units (group currency).
     * @param includedExactCents     Sum of group-currency cents for EXACT INCLUDED add-ons.
     * @param totalIncludedPercentage Combined percentage of PERCENTAGE INCLUDED add-ons
     *                                (e.g., 20 for 20 %).
     * @return The derived base cost in minor units, never negative.
     */
    fun calculateIncludedBaseCost(
        totalAmountCents: Long,
        includedExactCents: Long,
        totalIncludedPercentage: BigDecimal
    ): Long {
        val noExact = includedExactCents == 0L
        val noPercentage = totalIncludedPercentage.compareTo(BigDecimal.ZERO) == 0
        if (noExact && noPercentage) return totalAmountCents

        val afterExact = totalAmountCents - includedExactCents
        if (noPercentage) return afterExact.coerceAtLeast(0L)

        val percentFraction = totalIncludedPercentage.divide(
            BigDecimal("100"),
            RATE_PRECISION,
            RoundingMode.HALF_UP
        )
        val divisor = BigDecimal.ONE.add(percentFraction)
        return BigDecimal(afterExact)
            .divide(divisor, 0, RoundingMode.HALF_UP)
            .toLong()
            .coerceAtLeast(0L)
    }

    /**
     * Computes the effective deducted amount for a cash withdrawal, including ATM fee add-ons.
     *
     * ATM fee add-ons are ON_TOP by nature — they increase the real cost of the withdrawal
     * that should be reflected in the group's pocket balance.
     *
     * @param baseDeductedAmount The withdrawal's raw `deductedBaseAmount` (in group currency minor units).
     * @param addOns The structured add-ons list (typically a single ATM fee).
     * @return The effective deducted amount in minor units.
     */
    fun calculateEffectiveDeductedAmount(baseDeductedAmount: Long, addOns: List<AddOn>): Long {
        if (addOns.isEmpty()) return baseDeductedAmount
        val addOnTotal = addOns
            .filter { it.mode == AddOnMode.ON_TOP }
            .sumOf { it.groupAmountCents }
        return baseDeductedAmount + addOnTotal
    }

    // ── Exchange Rate Calculations ───────────────────────────────────────

    /**
     * Calculates the exchange rate between two amounts in their smallest currency units.
     *
     * Used to derive the rate from a cash withdrawal where the user withdrew a foreign
     * currency amount and the equivalent was deducted from the group pocket.
     *
     * @param amountWithdrawn The amount in the withdrawn currency (smallest units, e.g., cents).
     * @param deductedBaseAmount The equivalent amount deducted in the group's base currency (smallest units).
     * @return The exchange rate as BigDecimal (amountWithdrawn / deductedBaseAmount),
     *         or [BigDecimal.ONE] if deductedBaseAmount is zero or negative.
     */
    fun calculateExchangeRate(amountWithdrawn: Long, deductedBaseAmount: Long): BigDecimal {
        if (deductedBaseAmount <= 0) return BigDecimal.ONE
        return BigDecimal(amountWithdrawn)
            .divide(BigDecimal(deductedBaseAmount), RATE_PRECISION, RoundingMode.HALF_UP)
    }

    /**
     * Distributes a total amount equally among a number of users, ensuring
     * the sum of all allocations equals the total exactly (conservation of currency).
     *
     * Uses floor division to compute a base share, then distributes the
     * fractional remainder (in smallest currency units) sequentially to
     * the first users.
     *
     * Example: distributeAmount(BigDecimal("10.00"), 3, 2) →
     *   [BigDecimal("3.34"), BigDecimal("3.33"), BigDecimal("3.33")]
     *   Sum = 10.00 ✓
     *
     * @param totalAmount The total amount to distribute.
     * @param numberOfUsers The number of users to split among. Must be > 0.
     * @param decimalPlaces Number of decimal places for the currency (default 2).
     * @return A list of BigDecimal allocations whose sum equals totalAmount exactly.
     * @throws IllegalArgumentException if numberOfUsers <= 0.
     */
    fun distributeAmount(
        totalAmount: BigDecimal,
        numberOfUsers: Int,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): List<BigDecimal> {
        require(numberOfUsers > 0) { "Number of users must be greater than zero" }

        // Normalize totalAmount to the target scale to prevent sub-smallest-unit fractions
        val normalizedTotal = totalAmount.setScale(decimalPlaces, RoundingMode.HALF_UP)

        val divisor = BigDecimal(numberOfUsers)

        // Floor-divide: truncate (round down) to the target decimal places
        val baseShare = normalizedTotal.divide(divisor, decimalPlaces, RoundingMode.DOWN)

        // Remainder = total - (baseShare * numberOfUsers)
        val allocatedTotal = baseShare.multiply(divisor)
        val remainder = normalizedTotal.subtract(allocatedTotal)

        // Express remainder in smallest currency units (e.g., cents).
        // Use movePointRight for exact integer conversion and RoundingMode.DOWN
        // to guarantee extraUnits never exceeds the actual remainder.
        val extraUnits = remainder.movePointRight(decimalPlaces)
            .setScale(0, RoundingMode.DOWN)
            .intValueExact()

        // Build result: first `extraUnits` users get baseShare + 1 smallest unit
        val smallestUnit = BigDecimal.ONE.movePointLeft(decimalPlaces)
        return List(numberOfUsers) { index ->
            if (index < extraUnits) {
                baseShare.add(smallestUnit)
            } else {
                baseShare
            }
        }
    }

    /**
     * Computes the blended internal exchange rate from a FIFO cash expense result.
     *
     * Internal rate = groupAmountCents / sourceAmountCents
     * (i.e., "1 source unit = X group units").
     *
     * This is used to set a correct `exchangeRate` on cash expenses, replacing the
     * incorrect Open Exchange Rates API rate that the UI may have initially shown.
     *
     * @param sourceAmountCents The expense amount in the source (cash) currency, in cents.
     * @param groupAmountCents The blended cost in the group's base currency, in cents (from FIFO).
     * @return The blended internal rate, or [BigDecimal.ONE] if either input is non-positive.
     */
    fun calculateBlendedRate(sourceAmountCents: Long, groupAmountCents: Long): BigDecimal {
        if (sourceAmountCents <= 0 || groupAmountCents <= 0) return BigDecimal.ONE
        return BigDecimal(groupAmountCents)
            .divide(BigDecimal(sourceAmountCents), RATE_PRECISION, RoundingMode.HALF_UP)
    }

    /**
     * Computes the blended display exchange rate from a FIFO cash expense result.
     *
     * Display rate = sourceAmountCents / groupAmountCents
     * (i.e., "1 group unit = X source units", e.g., "1 EUR = 37.22 THB").
     *
     * This is the user-facing rate shown in the exchange rate section when the
     * payment method is CASH.
     *
     * @param sourceAmountCents The expense amount in the source (cash) currency, in cents.
     * @param groupAmountCents The blended cost in the group's base currency, in cents (from FIFO).
     * @return The blended display rate, or [BigDecimal.ONE] if either input is non-positive.
     */
    fun calculateBlendedDisplayRate(sourceAmountCents: Long, groupAmountCents: Long): BigDecimal {
        if (sourceAmountCents <= 0 || groupAmountCents <= 0) return BigDecimal.ONE
        return BigDecimal(sourceAmountCents)
            .divide(BigDecimal(groupAmountCents), RATE_PRECISION, RoundingMode.HALF_UP)
    }

    /**
     * Checks whether the available cash withdrawals are insufficient to cover the requested amount.
     *
     * @param amountToCover The expense amount in the cash currency (in cents).
     * @param availableWithdrawals List of withdrawals with remaining balance, ordered by createdAt asc.
     * @return true if total remaining is less than amountToCover.
     */
    fun hasInsufficientCash(amountToCover: Long, availableWithdrawals: List<CashWithdrawal>): Boolean {
        val totalAvailable = availableWithdrawals.sumOf { it.remainingAmount }
        return totalAvailable < amountToCover
    }

    /**
     * Applies the FIFO algorithm to determine how an expense should consume cash from
     * multiple ATM withdrawals, each with their own historical exchange rate.
     *
     * Iterates over withdrawals ordered by createdAt ascending (oldest first), deducting
     * from each until the expense is fully covered.
     *
     * @param amountToCover The total expense amount in the cash currency (in cents).
     * @param availableWithdrawals Withdrawals with remaining balance > 0, ordered by createdAt asc.
     * @param targetDecimalPlaces Decimal places for the group/base currency (default 2).
     * @return A [FifoCashResult] containing the blended base currency cost and the tranches consumed.
     * @throws IllegalStateException if available cash is insufficient.
     */
    fun calculateFifoCashAmount(
        amountToCover: Long,
        availableWithdrawals: List<CashWithdrawal>,
        targetDecimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): FifoCashResult {
        require(amountToCover > 0) { "Amount to cover must be greater than zero" }
        check(!hasInsufficientCash(amountToCover, availableWithdrawals)) {
            "Insufficient cash. Required: $amountToCover, Available: ${availableWithdrawals.sumOf {
                it.remainingAmount
            }}"
        }

        var remaining = amountToCover
        val tranches = mutableListOf<CashTranche>()
        var totalBaseAmountCents = 0L

        for (withdrawal in availableWithdrawals) {
            if (remaining <= 0) break

            val consumed = minOf(remaining, withdrawal.remainingAmount)
            tranches.add(
                CashTranche(
                    withdrawalId = withdrawal.id,
                    amountConsumed = consumed
                )
            )

            // Calculate the base currency equivalent for this tranche using
            // the withdrawal's historical exchange rate.
            // rate = deductedBaseAmount / amountWithdrawn (base per cash unit)
            val rate = if (withdrawal.amountWithdrawn > 0) {
                BigDecimal(withdrawal.deductedBaseAmount)
                    .divide(BigDecimal(withdrawal.amountWithdrawn), RATE_PRECISION, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            val baseAmountForTranche = BigDecimal(consumed)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()

            totalBaseAmountCents += baseAmountForTranche
            remaining -= consumed
        }

        return FifoCashResult(
            groupAmountCents = totalBaseAmountCents,
            tranches = tranches
        )
    }

    /**
     * Result of a FIFO cash calculation.
     *
     * @param groupAmountCents The blended cost in the group's base currency (in cents).
     * @param tranches The specific withdrawal portions consumed by this expense.
     */
    data class FifoCashResult(val groupAmountCents: Long, val tranches: List<CashTranche>)
}
