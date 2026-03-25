package es.pedrazamiguez.expenseshareapp.domain.service.addon

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Resolves an EXACT add-on amount: converts the user's input directly
 * from the major currency unit to the smallest unit (cents).
 *
 * Example: input "5.50" with decimalDigits=2 → 550 cents.
 */
class ExactAddOnResolver : AddOnAmountResolver {

    @Suppress("UNUSED_PARAMETER")
    override fun resolve(
        normalizedInput: BigDecimal,
        decimalDigits: Int,
        sourceAmountCents: Long
    ): Long {
        val multiplier = BigDecimal.TEN.pow(decimalDigits)
        return normalizedInput.multiply(multiplier)
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()
    }
}
