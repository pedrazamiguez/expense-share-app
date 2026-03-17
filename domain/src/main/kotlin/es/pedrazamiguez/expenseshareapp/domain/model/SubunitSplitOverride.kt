package es.pedrazamiguez.expenseshareapp.domain.model

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType

/**
 * Per-sub-unit Level 2 override for intra-sub-unit splitting.
 *
 * When present in [SubunitAwareSplitService.calculateShares], this override
 * replaces the default proportional distribution (based on [Subunit.memberShares])
 * with an explicit split strategy and per-member amounts.
 *
 * @param splitType  How to split within this sub-unit (EQUAL, EXACT, or PERCENT).
 * @param memberSplits  Per-member split details. Required for EXACT and PERCENT;
 *                      ignored for EQUAL (auto-calculated).
 */
data class SubunitSplitOverride(
    val splitType: SplitType,
    val memberSplits: List<ExpenseSplit> = emptyList()
)

