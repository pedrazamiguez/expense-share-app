package es.pedrazamiguez.expenseshareapp.domain.service.addon

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Resolves a PERCENTAGE add-on amount: computes the percentage
 * of the expense's source amount.
 *
 * Example: input "10" (10%) with sourceAmountCents=10000 → 1000 cents.
 */
class PercentageAddOnResolver : AddOnAmountResolver {

    @Suppress("UNUSED_PARAMETER")
    override fun resolve(
        normalizedInput: BigDecimal,
        decimalDigits: Int,
        sourceAmountCents: Long
    ): Long {
        if (sourceAmountCents <= 0) return 0L
        return BigDecimal(sourceAmountCents)
            .multiply(normalizedInput)
            .divide(BigDecimal(HUNDRED), 0, RoundingMode.HALF_UP)
            .toLong()
    }

    private companion object {
        const val HUNDRED = 100
    }
}
