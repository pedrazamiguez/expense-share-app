package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.SplitType

/**
 * Per-subunit Level 2 override for intra-subunit splitting.
 *
 * When present in [SubunitAwareSplitService.calculateShares], this override
 * replaces the default proportional distribution (based on [Subunit.memberShares])
 * with an explicit split strategy and per-member amounts.
 *
 * @param splitType  How to split within this subunit (EQUAL, EXACT, or PERCENT).
 * @param memberSplits  Per-member split details. Required for EXACT and PERCENT;
 *                      ignored for EQUAL (auto-calculated).
 */
data class SubunitSplitOverride(
    val splitType: SplitType,
    val memberSplits: List<ExpenseSplit> = emptyList()
)
