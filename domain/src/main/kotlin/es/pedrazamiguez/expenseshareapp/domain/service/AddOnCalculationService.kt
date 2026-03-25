package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.service.addon.AddOnResolverFactory
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for add-on-specific calculations: resolution, totalling, effective
 * amounts (with add-ons), and included-base-cost decomposition.
 *
 * Extracted from [ExpenseCalculatorService] to adhere to the single-responsibility
 * principle and keep each service below the configured function-count threshold.
 */
class AddOnCalculationService(
    private val addOnResolverFactory: AddOnResolverFactory = AddOnResolverFactory()
) {

    private companion object {
        const val RATE_PRECISION = 6
    }

    // ── Add-On Amount Resolution ────────────────────────────────────────

    /**
     * Resolves an add-on's user input into the absolute amount in the add-on's
     * own currency (minor units / cents).
     *
     * For [AddOnValueType.EXACT], converts the normalized input to cents directly
     * (`input × 10^decimalDigits`).
     *
     * For [AddOnValueType.PERCENTAGE], computes the percentage of the source amount:
     * `sourceAmountCents × input / 100`.
     *
     * @param normalizedInput  The user input already normalized to a parseable [BigDecimal]
     *                         (e.g., via [CurrencyConverter.normalizeAmountString]).
     * @param valueType        Whether the user entered an exact amount or a percentage.
     * @param decimalDigits    Number of decimal places for the add-on's currency.
     * @param sourceAmountCents The expense's source amount in cents — used only for
     *                          [AddOnValueType.PERCENTAGE].
     * @return The resolved amount in minor units, or 0 if [sourceAmountCents] is non-positive
     *         when value type is PERCENTAGE.
     */
    fun resolveAddOnAmountCents(
        normalizedInput: BigDecimal,
        valueType: AddOnValueType,
        decimalDigits: Int,
        sourceAmountCents: Long
    ): Long = addOnResolverFactory.create(valueType).resolve(
        normalizedInput = normalizedInput,
        decimalDigits = decimalDigits,
        sourceAmountCents = sourceAmountCents
    )

    // ── Add-On Totals ───────────────────────────────────────────────────

    /**
     * Sums the [AddOn.groupAmountCents] of all ON_TOP add-ons that are NOT discounts.
     *
     * Returns only ON_TOP extras (fees, tips, surcharges added on top of the base).
     * INCLUDED add-ons are excluded because they are part of the original total
     * (extracted from it), not additional costs.
     * Discounts are handled separately in [calculateEffectiveGroupAmount].
     *
     * @param addOns The list of add-ons attached to an expense.
     * @return The total group-currency amount of on-top, non-discount add-ons.
     */
    fun calculateTotalOnTopAddOns(addOns: List<AddOn>): Long =
        addOns.filter { it.mode == AddOnMode.ON_TOP && it.type != AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

    /**
     * Sums the [AddOn.groupAmountCents] of ALL non-discount add-ons, regardless of mode.
     *
     * This is the value shown as "Extras" in the group balance screen — it covers both
     * ON_TOP add-ons (e.g., ATM fee, bank fee) and INCLUDED add-ons (e.g., tip already
     * embedded in the total). Discounts are excluded because they reduce the price
     * paid but are not extra costs to surface in the summary.
     *
     * Contrast with [calculateTotalOnTopAddOns] which only counts ON_TOP mode.
     *
     * @param addOns The list of add-ons attached to an expense.
     * @return The total group-currency amount of all non-discount add-ons.
     */
    fun calculateTotalAddOnExtras(addOns: List<AddOn>): Long =
        addOns.filter { it.type != AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

    // ── Effective Amount Calculations ────────────────────────────────────

    /**
     * Computes the effective group debt for an expense, accounting for add-ons.
     *
     * Formula: `baseGroupAmount + ON_TOP (non-discount) + INCLUDED (non-discount) − DISCOUNT`
     *
     * - **ON_TOP** add-ons (fees, tips, surcharges) increase the total.
     * - **INCLUDED** add-ons reconstruct the original user-entered total from the
     *   decomposed base cost stored in [baseGroupAmount].
     * - **DISCOUNT** add-ons reduce the total.
     *
     * Both ON_TOP and INCLUDED decompose the payment into **base + add-on**.
     * The only difference is the input flow: ON_TOP adds on top of the base,
     * INCLUDED extracts from the user-entered total to derive the base.
     *
     * When [addOns] is empty the result equals [baseGroupAmount] — no behavioral
     * change for expenses without add-ons.
     *
     * @param baseGroupAmount The expense's `groupAmount` (base cost, in minor units).
     * @param addOns The structured add-ons list.
     * @return The effective group amount in minor units.
     */
    fun calculateEffectiveGroupAmount(baseGroupAmount: Long, addOns: List<AddOn>): Long {
        if (addOns.isEmpty()) return baseGroupAmount

        val onTop = addOns
            .filter { it.mode == AddOnMode.ON_TOP && it.type != AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

        val included = addOns
            .filter { it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

        val discounts = addOns
            .filter { it.type == AddOnType.DISCOUNT }
            .sumOf { it.groupAmountCents }

        return (baseGroupAmount + onTop + included - discounts).coerceAtLeast(0L)
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

        // Guard against non-positive divisors (e.g., user enters -100% → divisor = 0)
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            return afterExact.coerceAtLeast(0L)
        }

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

    // ── Utility ─────────────────────────────────────────────────────────

    /**
     * Converts an amount in group currency cents back to the add-on's own currency
     * using the add-on's exchange rate.
     *
     * `result = groupAmountCents / exchangeRate`
     *
     * @param groupAmountCents The amount in the group currency's minor units.
     * @param exchangeRate     The add-on's exchange rate (add-on currency → group currency).
     * @return The equivalent amount in the add-on's own currency, or [groupAmountCents]
     *         if the exchange rate is zero.
     */
    fun convertGroupToSourceCents(groupAmountCents: Long, exchangeRate: BigDecimal): Long {
        if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) return groupAmountCents
        return BigDecimal(groupAmountCents)
            .divide(exchangeRate, 0, RoundingMode.HALF_UP)
            .toLong()
    }

    /**
     * Sums the parsed percentage values from a list of raw amount input strings.
     *
     * Each input is normalized via [CurrencyConverter.normalizeAmountString] and
     * parsed to [BigDecimal]. Unparseable inputs are treated as zero.
     *
     * Centralizes the `fold(BigDecimal.ZERO) { acc, … → acc.add(…) }` pattern
     * that was previously duplicated in presentation-layer handlers.
     *
     * @param amountInputs The raw user-entered percentage strings (may contain locale separators).
     * @return The sum of all parseable percentages.
     */
    fun sumPercentagesFromInputs(amountInputs: List<String>): BigDecimal =
        amountInputs.fold(BigDecimal.ZERO) { acc, input ->
            val normalized = CurrencyConverter.normalizeAmountString(input.trim())
            acc.add(normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO)
        }
}
