package es.pedrazamiguez.expenseshareapp.domain.service

/**
 * Lightweight domain service for sub-unit share percentage math.
 *
 * Unlike [es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService],
 * this operates purely on `Double` shares (0.0–1.0) without any `amountCents`
 * or `BigDecimal` coupling, matching the [es.pedrazamiguez.expenseshareapp.domain.model.Subunit.memberShares]
 * contract directly.
 *
 * Responsibilities:
 * - Even share distribution among N members.
 * - Remaining-share redistribution when one member's share is manually edited.
 * - Parsing raw user input (percentage text) into domain-level share maps.
 */
class SubunitShareDistributionService {

    companion object {
        private const val SHARE_SUM_TOLERANCE = 0.0001
    }

    /**
     * Distributes shares evenly among [memberIds].
     *
     * @return Map of userId → share where all values sum to 1.0.
     *         E.g., 3 members → {A: 0.3333…, B: 0.3333…, C: 0.3333…}
     */
    fun distributeEvenly(memberIds: List<String>): Map<String, Double> {
        if (memberIds.isEmpty()) return emptyMap()
        val equalShare = 1.0 / memberIds.size
        return memberIds.associateWith { equalShare }
    }

    /**
     * Redistributes the remaining share (1.0 − [editedShare]) evenly among
     * [otherMemberIds] when a user manually edits their own share.
     *
     * @param editedShare The share value (0.0–1.0) the user typed.
     * @param otherMemberIds The other selected members (excluding the editor).
     * @return Map of userId → share for the other members.
     *         Returns empty map if [otherMemberIds] is empty.
     */
    fun redistributeRemaining(
        editedShare: Double,
        otherMemberIds: List<String>
    ): Map<String, Double> {
        if (otherMemberIds.isEmpty()) return emptyMap()

        val remaining = (1.0 - editedShare).coerceAtLeast(0.0)
        val otherShare = remaining / otherMemberIds.size
        return otherMemberIds.associateWith { otherShare }
    }

    /**
     * Parses user-entered percentage text values into domain-level share doubles (0.0–1.0).
     *
     * - If all entries are blank or map is empty, returns empty map
     *   (signals auto-normalization to [SubunitValidationService]).
     * - If any selected member has an unparseable non-blank entry,
     *   returns empty map (fall back to auto-normalization).
     * - Otherwise converts each "50" → 0.5, "33.33" → 0.3333, etc.
     *
     * @param selectedMemberIds The currently selected member IDs.
     * @param memberShareTexts Map of userId → raw percentage text from the form.
     * @return Map of userId → share (0.0–1.0), or empty map for auto-normalization.
     */
    fun parseShareTexts(
        selectedMemberIds: List<String>,
        memberShareTexts: Map<String, String>
    ): Map<String, Double> {
        if (memberShareTexts.isEmpty()) return emptyMap()

        val allBlank = memberShareTexts.values.all { it.isBlank() }
        if (allBlank) return emptyMap()

        val parsed = selectedMemberIds.associate { userId ->
            val shareText = memberShareTexts[userId] ?: ""
            val shareValue = shareText.toDoubleOrNull()?.div(100.0)
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

        return parsed.mapValues { it.value ?: 0.0 }
    }

    /**
     * Formats a domain share value (0.0–1.0) as a percentage string for input fields.
     * E.g., 0.5 → "50", 0.3333 → "33.33"
     */
    fun formatShareForInput(share: Double): String {
        val percent = share * 100
        return if (percent == percent.toLong().toDouble()) {
            percent.toLong().toString()
        } else {
            String.format("%.2f", percent).trimEnd('0').trimEnd('.')
        }
    }
}

