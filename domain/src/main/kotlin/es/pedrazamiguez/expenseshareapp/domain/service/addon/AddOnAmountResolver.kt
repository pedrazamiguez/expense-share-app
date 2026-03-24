package es.pedrazamiguez.expenseshareapp.domain.service.addon

import java.math.BigDecimal

/**
 * Strategy interface for resolving add-on user input into an absolute
 * amount in the add-on's own currency (minor units / cents).
 *
 * Implementations handle [AddOnValueType.EXACT] and [AddOnValueType.PERCENTAGE]
 * resolution logic independently, following the Strategy pattern
 * (mirrors [ExpenseSplitCalculator] / [ExpenseSplitCalculatorFactory]).
 */
interface AddOnAmountResolver {

    /**
     * Resolves the user's normalized input into the absolute amount in cents.
     *
     * @param normalizedInput    The user input as a parsed [BigDecimal].
     * @param decimalDigits      Number of decimal places for the add-on's currency.
     * @param sourceAmountCents  The expense's source amount in cents (used by percentage resolver).
     * @return The resolved amount in the smallest currency unit (cents).
     */
    fun resolve(
        normalizedInput: BigDecimal,
        decimalDigits: Int,
        sourceAmountCents: Long
    ): Long
}
