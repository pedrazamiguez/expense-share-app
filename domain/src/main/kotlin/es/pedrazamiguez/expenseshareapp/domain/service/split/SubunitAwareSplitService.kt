package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.EntitySplit
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.SubunitSplitOverride
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Domain service that orchestrates two-level sub-unit-aware expense splitting.
 *
 * **Level 1 — Entity-Level Split:**
 * How the total expense is divided among "entities" (solo travelers + sub-units as single units).
 *
 * **Level 2 — Intra-Sub-Unit Split:**
 * How each sub-unit's share is divided among its members.
 *
 * Both levels support all three split strategies (EQUAL, EXACT, PERCENT).
 * The output is always a **flat** `List<ExpenseSplit>` (one entry per actual user),
 * suitable for direct storage in Room/Firestore.
 *
 * All decimal math uses [BigDecimal] with explicit [RoundingMode] and scale.
 */
class SubunitAwareSplitService(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory
) {


    /**
     * Two-level split: first at entity level, then within each sub-unit.
     *
     * @param totalAmountCents          Total expense amount in the smallest currency unit.
     * @param individualParticipantIds  User IDs NOT in any sub-unit (solo travelers).
     * @param subunits                  Sub-units participating in the split.
     * @param entitySplitType           How to split among entities (EQUAL, EXACT, PERCENT).
     * @param entitySplits              Pre-existing entity-level splits (for EXACT/PERCENT at Level 1).
     * @param subunitSplitOverrides     Per-sub-unit override for intra-sub-unit splitting.
     *                                  Key = subunitId. If absent, uses [Subunit.memberShares].
     * @return Flattened list of per-user [ExpenseSplit] entries.
     */
    fun calculateShares(
        totalAmountCents: Long,
        individualParticipantIds: List<String>,
        subunits: List<Subunit>,
        entitySplitType: SplitType,
        entitySplits: List<EntitySplit> = emptyList(),
        subunitSplitOverrides: Map<String, SubunitSplitOverride> = emptyMap()
    ): List<ExpenseSplit> {
        // Edge case: no sub-units → delegate to flat splitting (current behavior)
        if (subunits.isEmpty()) {
            return calculateFlatSplit(
                totalAmountCents, individualParticipantIds, entitySplitType, entitySplits
            )
        }

        // Step 1: Build entity list — solo user IDs + sub-unit IDs
        val entityIds = individualParticipantIds + subunits.map { it.id }

        // Step 2: Compute entity-level shares using the calculator factory
        val entityLevelSplits = calculateEntityLevelSplits(
            totalAmountCents, entityIds, entitySplitType, entitySplits
        )

        // Step 3: Build a lookup of entity shares by entity ID
        val entityShareMap = entityLevelSplits.associateBy { it.userId }

        // Step 4: Expand into per-user splits
        val result = mutableListOf<ExpenseSplit>()

        // Solo participants — pass through directly (no subunitId)
        for (userId in individualParticipantIds) {
            val entityShare = entityShareMap[userId]
            if (entityShare != null) {
                result.add(
                    ExpenseSplit(
                        userId = userId,
                        amountCents = entityShare.amountCents,
                        percentage = entityShare.percentage,
                        subunitId = null
                    )
                )
            }
        }

        // Sub-units — expand each sub-unit's entity share into per-member splits
        for (subunit in subunits) {
            val entityShare = entityShareMap[subunit.id] ?: continue
            val memberSplits = expandSubunitShare(
                subunit = subunit,
                subunitShareCents = entityShare.amountCents,
                override = subunitSplitOverrides[subunit.id]
            )
            result.addAll(memberSplits)
        }

        // Step 5: Ensure percentage consistency
        // When entity-level split is PERCENT, compute effective per-user percentages
        // for sub-unit members so all returned splits are self-describing.
        if (entitySplitType == SplitType.PERCENT && totalAmountCents > 0) {
            val totalBd = BigDecimal(totalAmountCents)
            val hundredBd = BigDecimal(100)
            return result.map { split ->
                if (split.percentage == null) {
                    split.copy(
                        percentage = BigDecimal(split.amountCents)
                            .multiply(hundredBd)
                            .divide(totalBd, 2, RoundingMode.HALF_UP)
                    )
                } else {
                    split
                }
            }
        }

        return result
    }

    /**
     * Delegates to the existing flat splitting logic when no sub-units are involved.
     * This ensures backward compatibility — groups without sub-units behave identically.
     */
    private fun calculateFlatSplit(
        totalAmountCents: Long,
        participantIds: List<String>,
        splitType: SplitType,
        entitySplits: List<EntitySplit>
    ): List<ExpenseSplit> {
        val calculator = splitCalculatorFactory.create(splitType)
        val existingSplits = entitySplits.map { entitySplit ->
            ExpenseSplit(
                userId = entitySplit.entityId,
                amountCents = entitySplit.amountCents,
                percentage = entitySplit.percentage
            )
        }
        return calculator.calculateShares(totalAmountCents, participantIds, existingSplits)
    }

    /**
     * Computes Level 1 entity-level shares by treating each entity (solo user or sub-unit)
     * as a single "participant" and delegating to the appropriate [ExpenseSplitCalculator].
     */
    private fun calculateEntityLevelSplits(
        totalAmountCents: Long,
        entityIds: List<String>,
        entitySplitType: SplitType,
        entitySplits: List<EntitySplit>
    ): List<ExpenseSplit> {
        val calculator = splitCalculatorFactory.create(entitySplitType)
        val existingSplits = entitySplits.map { entitySplit ->
            ExpenseSplit(
                userId = entitySplit.entityId,
                amountCents = entitySplit.amountCents,
                percentage = entitySplit.percentage
            )
        }
        return calculator.calculateShares(totalAmountCents, entityIds, existingSplits)
    }

    /**
     * Expands a sub-unit's entity-level share into per-member [ExpenseSplit] entries.
     *
     * If an [override] is provided, it dictates the intra-sub-unit split strategy.
     * Otherwise, the sub-unit's [Subunit.memberShares] are used for proportional distribution.
     */
    private fun expandSubunitShare(
        subunit: Subunit,
        subunitShareCents: Long,
        override: SubunitSplitOverride?
    ): List<ExpenseSplit> {
        return if (override != null) {
            expandWithOverride(subunit, subunitShareCents, override)
        } else {
            expandByMemberShares(subunit, subunitShareCents)
        }
    }

    /**
     * Expands using an explicit override (Level 2 with a specific split strategy).
     */
    private fun expandWithOverride(
        subunit: Subunit,
        subunitShareCents: Long,
        override: SubunitSplitOverride
    ): List<ExpenseSplit> {
        val calculator = splitCalculatorFactory.create(override.splitType)
        val memberIds = subunit.memberIds

        val existingSplits = override.memberSplits.map { memberSplit ->
            ExpenseSplit(
                userId = memberSplit.userId,
                amountCents = memberSplit.amountCents,
                percentage = memberSplit.percentage
            )
        }

        val memberSplits = calculator.calculateShares(
            subunitShareCents, memberIds, existingSplits
        )

        return memberSplits.map { split ->
            split.copy(subunitId = subunit.id)
        }
    }

    /**
     * Distributes the sub-unit's share proportionally based on [Subunit.memberShares].
     *
     * If [Subunit.memberShares] is empty, falls back to equal distribution.
     *
     * Uses BigDecimal math with remainder distribution to ensure the member amounts
     * sum exactly to [subunitShareCents].
     */
    private fun expandByMemberShares(
        subunit: Subunit,
        subunitShareCents: Long
    ): List<ExpenseSplit> {
        val memberIds = subunit.memberIds

        // If no explicit shares, fall back to equal split
        if (subunit.memberShares.isEmpty()) {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            return calculator.calculateShares(subunitShareCents, memberIds).map { split ->
                split.copy(subunitId = subunit.id)
            }
        }

        // Proportional distribution using memberShares weights
        val totalShareBd = BigDecimal(subunitShareCents)
        val rawAmounts = memberIds.map { userId ->
            val weight = subunit.memberShares[userId] ?: BigDecimal.ZERO
            val rawAmount = totalShareBd.multiply(weight)
                .setScale(0, RoundingMode.DOWN)
                .toLong()
            userId to rawAmount
        }

        // Distribute remainder (cents lost to rounding)
        val allocatedTotal = rawAmounts.sumOf { it.second }
        var remainder = subunitShareCents - allocatedTotal

        return rawAmounts.map { (userId, rawAmount) ->
            val extraCent = if (remainder > 0) {
                remainder--
                1L
            } else {
                0L
            }
            ExpenseSplit(
                userId = userId,
                amountCents = rawAmount + extraCent,
                subunitId = subunit.id
            )
        }
    }
}

