package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.SplitPreviewShare
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Domain service that encapsulates the **live preview** math for expense splits.
 *
 * This handles ephemeral UI-feedback calculations — what to display in other
 * input fields as the user types in real time. The authoritative split
 * calculations at save time remain in [ExpenseSplitCalculator] strategies.
 *
 * Responsibilities:
 * - Even percentage distribution with remainder handling.
 * - Remaining-percentage redistribution when one user edits their share.
 * - Amount-from-percentage derivation.
 * - Locale-aware amount parsing to cents / BigDecimal.
 */
class SplitPreviewService {

    companion object {
        private val HUNDRED = BigDecimal("100")
        private val SMALLEST_PERCENT_UNIT = BigDecimal("0.01")
        private const val PERCENT_SCALE = 2
        private const val DEFAULT_DECIMAL_PLACES = 2
    }

    // ── Percentage Distribution ─────────────────────────────────────────

    /**
     * Distributes 100 % evenly among [participantIds] and computes the
     * corresponding [SplitPreviewShare.amountCents] from [sourceAmountCents].
     *
     * Remainder cents (from rounding 100 / N down to 2 dp) are distributed
     * one-by-one to the first participants so percentages always sum to 100.00.
     *
     * @param sourceAmountCents Total expense amount in smallest currency unit.
     * @param participantIds    Active (non-excluded) participant user IDs.
     * @return One [SplitPreviewShare] per participant, ordered as [participantIds].
     */
    fun distributePercentagesEvenly(sourceAmountCents: Long, participantIds: List<String>): List<SplitPreviewShare> {
        if (participantIds.isEmpty()) return emptyList()

        val count = participantIds.size
        val basePercent = HUNDRED.divide(BigDecimal(count), PERCENT_SCALE, RoundingMode.DOWN)

        val allocatedPercent = basePercent.multiply(BigDecimal(count))
        var remainderUnits = HUNDRED.subtract(allocatedPercent)
            .movePointRight(PERCENT_SCALE)
            .setScale(0, RoundingMode.DOWN)
            .toInt()

        val shares = participantIds.map { userId ->
            val pct = if (remainderUnits > 0) {
                remainderUnits--
                basePercent.add(SMALLEST_PERCENT_UNIT)
            } else {
                basePercent
            }
            SplitPreviewShare(
                userId = userId,
                amountCents = calculateAmountFromPercentage(pct, sourceAmountCents),
                percentage = pct
            )
        }

        return distributeAmountRemainder(shares, sourceAmountCents)
    }

    /**
     * Redistributes the remaining percentage (100 − [editedPercentage]) evenly
     * among [otherParticipantIds] and computes their preview amounts.
     *
     * Called when a user manually types a percentage for their share — the
     * remaining percentage is spread across the other active members.
     *
     * @param editedPercentage     The percentage the user typed.
     * @param sourceAmountCents    Total expense amount in smallest currency unit.
     * @param otherParticipantIds  The other active participants (excluding the editor).
     * @return One [SplitPreviewShare] per other participant.
     */
    fun redistributeRemainingPercentage(
        editedPercentage: BigDecimal,
        sourceAmountCents: Long,
        otherParticipantIds: List<String>
    ): List<SplitPreviewShare> {
        if (otherParticipantIds.isEmpty()) return emptyList()

        val remainingPct = HUNDRED.subtract(editedPercentage).coerceAtLeast(BigDecimal.ZERO)
        val otherCount = otherParticipantIds.size
        val otherBasePct = remainingPct.divide(BigDecimal(otherCount), PERCENT_SCALE, RoundingMode.DOWN)

        val allocatedOtherPct = otherBasePct.multiply(BigDecimal(otherCount))
        var remainderUnits = remainingPct.subtract(allocatedOtherPct)
            .movePointRight(PERCENT_SCALE)
            .setScale(0, RoundingMode.DOWN)
            .toInt()

        val shares = otherParticipantIds.map { userId ->
            val pct = if (remainderUnits > 0) {
                remainderUnits--
                otherBasePct.add(SMALLEST_PERCENT_UNIT)
            } else {
                otherBasePct
            }
            SplitPreviewShare(
                userId = userId,
                amountCents = calculateAmountFromPercentage(pct, sourceAmountCents),
                percentage = pct
            )
        }

        // Remainder for redistributed shares is relative to the remaining amount,
        // not the full sourceAmountCents, because the edited user's share is excluded.
        val remainingAmountCents = shares.sumOf { it.amountCents }
        val expectedRemainingCents =
            sourceAmountCents - calculateAmountFromPercentage(editedPercentage, sourceAmountCents)
        return distributeAmountRemainder(shares, expectedRemainingCents, remainingAmountCents)
    }

    // ── Amount Calculation ───────────────────────────────────────────────

    /**
     * Computes the amount in cents that corresponds to a given [percentage]
     * of [sourceAmountCents].
     *
     * Uses [RoundingMode.DOWN] to match the rounding strategy used by the
     * existing split calculators.
     *
     * @param percentage        The share percentage (e.g., 33.33).
     * @param sourceAmountCents The total expense amount in smallest currency unit.
     * @return The derived amount in cents.
     */
    fun calculateAmountFromPercentage(percentage: BigDecimal, sourceAmountCents: Long): Long {
        if (sourceAmountCents <= 0) return 0L
        return sourceAmountCents.toBigDecimal()
            .multiply(percentage)
            .divide(HUNDRED, 0, RoundingMode.DOWN)
            .toLong()
    }

    // ── Parsing Helpers ─────────────────────────────────────────────────

    /**
     * Parses a locale-aware amount input string to cents (Long).
     *
     * Uses [CurrencyConverter.normalizeAmountString] to handle different decimal
     * separators (comma vs. dot), then scales by [decimalDigits] to convert to
     * the smallest currency unit.
     *
     * @param input         The raw input string (e.g., "10,50" or "10.50").
     * @param decimalDigits Number of decimal places for the currency (0 for JPY, 2 for EUR, 3 for TND).
     * @return Amount in smallest currency unit, or 0 if parsing fails.
     */
    fun parseAmountToCents(input: String, decimalDigits: Int = DEFAULT_DECIMAL_PLACES): Long = try {
        val normalized = CurrencyConverter.normalizeAmountString(input.trim())
        val amount = normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
        amount.movePointRight(decimalDigits).setScale(0, RoundingMode.HALF_UP).toLong()
    } catch (_: Exception) {
        0L
    }

    /**
     * Parses a locale-aware decimal input string to [BigDecimal].
     *
     * @param input The raw input string.
     * @return The parsed decimal value, or [BigDecimal.ZERO] if parsing fails.
     */
    fun parseToDecimal(input: String): BigDecimal = try {
        val normalized = CurrencyConverter.normalizeAmountString(input.trim())
        normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
    } catch (_: Exception) {
        BigDecimal.ZERO
    }

    // ── Private helpers ─────────────────────────────────────────────────

    /**
     * Distributes orphan cents that were lost to rounding (DOWN) when converting
     * percentages to amounts. Grants one extra cent to the first participants
     * until the total matches [expectedTotalCents].
     *
     * This mirrors the remainder distribution logic used by [PercentSplitCalculator]
     * at save time, ensuring the preview amounts always sum correctly.
     */
    private fun distributeAmountRemainder(
        shares: List<SplitPreviewShare>,
        expectedTotalCents: Long,
        currentTotalCents: Long = shares.sumOf { it.amountCents }
    ): List<SplitPreviewShare> {
        var remainder = expectedTotalCents - currentTotalCents
        if (remainder <= 0) return shares

        return shares.map { share ->
            val extraCent = if (remainder > 0) {
                remainder--
                1L
            } else {
                0L
            }
            if (extraCent > 0) share.copy(amountCents = share.amountCents + extraCent) else share
        }
    }

    private fun BigDecimal.coerceAtLeast(min: BigDecimal): BigDecimal = if (this < min) min else this
}
