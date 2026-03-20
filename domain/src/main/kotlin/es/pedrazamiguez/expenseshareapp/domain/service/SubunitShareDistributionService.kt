package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Lightweight domain service for sub-unit share percentage math.
 *
 * All decimal arithmetic uses [BigDecimal] internally (with explicit
 * [RoundingMode] and scale) to avoid IEEE 754 floating-point drift.
 *
 * Returns [BigDecimal] values directly since
 * [es.pedrazamiguez.expenseshareapp.domain.model.Subunit.memberShares]
 * is now `Map<String, BigDecimal>`.
 *
 * Responsibilities:
 * - Even share distribution among N members.
 * - Remaining-share redistribution when one member's share is manually edited.
 * - Parsing raw user input (percentage text) into domain-level share maps.
 */
class SubunitShareDistributionService {

    companion object {
        private const val SHARE_SCALE = 10
        private val ONE = BigDecimal.ONE
        private val HUNDRED = BigDecimal("100")
    }

    /**
     * Distributes shares evenly among [memberIds].
     *
     * Uses [BigDecimal] for precise division.
     *
     * @return Map of userId → share where all values sum to ~1.
     *         E.g., 3 members → {A: 0.3333333333, B: 0.3333333333, C: 0.3333333333}
     */
    fun distributeEvenly(memberIds: List<String>): Map<String, BigDecimal> {
        if (memberIds.isEmpty()) return emptyMap()

        val count = BigDecimal(memberIds.size)
        val equalShare = ONE.divide(count, SHARE_SCALE, RoundingMode.DOWN)

        return memberIds.associateWith { equalShare }
    }

    /**
     * Redistributes the remaining share (1 − [editedShare] − sum([lockedShares]))
     * evenly among [otherMemberIds] when a user manually edits their own share.
     *
     * Locked members (whose shares were previously set by the user) are excluded
     * from redistribution. Their share values are subtracted from the remaining
     * budget before the even split.
     *
     * @param editedShare The share value (0–1) the user typed, as [BigDecimal].
     * @param otherMemberIds The other selected members (excluding the editor).
     * @param lockedShares Map of userId → locked share (0–1) for members whose
     *                     values should not be overwritten. Default empty (backward-compatible).
     * @return Map of userId → share for the unlocked other members.
     *         Returns empty map if there are no unlocked other members.
     */
    fun redistributeRemaining(
        editedShare: BigDecimal,
        otherMemberIds: List<String>,
        lockedShares: Map<String, BigDecimal> = emptyMap()
    ): Map<String, BigDecimal> {
        if (otherMemberIds.isEmpty()) return emptyMap()

        // Only consider locked shares for members that are actually in otherMemberIds
        val otherMemberIdSet = otherMemberIds.toSet()
        val filteredLockedShares = lockedShares.filterKeys { it in otherMemberIdSet }

        val lockedTotal = filteredLockedShares.values.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }
        val remaining = ONE.subtract(editedShare).subtract(lockedTotal).coerceAtLeast(BigDecimal.ZERO)

        val unlockedIds = otherMemberIds.filter { it !in filteredLockedShares }
        if (unlockedIds.isEmpty()) return emptyMap()

        val count = BigDecimal(unlockedIds.size)
        val otherShare = remaining.divide(count, SHARE_SCALE, RoundingMode.DOWN)

        return unlockedIds.associateWith { otherShare }
    }

    /**
     * Parses user-entered percentage text values into domain-level share [BigDecimal]s (0–1).
     *
     * Uses [CurrencyConverter.normalizeAmountString] to handle locale-specific
     * decimal separators (e.g., "33,33" → "33.33") before parsing.
     *
     * - If all entries are blank or map is empty, returns empty map
     *   (signals auto-normalization to [SubunitValidationService]).
     * - If any selected member has an unparseable non-blank entry,
     *   returns empty map (fall back to auto-normalization).
     * - Otherwise converts each "50" → 0.5, "33.33" → 0.3333, etc.
     *
     * @param selectedMemberIds The currently selected member IDs.
     * @param memberShareTexts Map of userId → raw percentage text from the form.
     * @return Map of userId → share (0–1), or empty map for auto-normalization.
     */
    fun parseShareTexts(
        selectedMemberIds: List<String>,
        memberShareTexts: Map<String, String>
    ): Map<String, BigDecimal> {
        if (memberShareTexts.isEmpty()) return emptyMap()

        val allBlank = memberShareTexts.values.all { it.isBlank() }
        if (allBlank) return emptyMap()

        val parsed = selectedMemberIds.associate { userId ->
            val shareText = memberShareTexts[userId] ?: ""
            val normalized = CurrencyConverter.normalizeAmountString(shareText)
            val shareValue = normalized.toBigDecimalOrNull()
                ?.divide(HUNDRED, SHARE_SCALE, RoundingMode.HALF_UP)
            userId to shareValue
        }

        // If any selected member has an unparseable (non-blank) entry,
        // fall back to auto-normalization rather than silently using 0.
        if (parsed.any { (userId, value) ->
                value == null && memberShareTexts[userId]?.isNotBlank() == true
            }
        ) {
            return emptyMap()
        }

        return parsed.mapValues { it.value ?: BigDecimal.ZERO }
    }

    private fun BigDecimal.coerceAtLeast(minimum: BigDecimal): BigDecimal = if (this < minimum) minimum else this
}
