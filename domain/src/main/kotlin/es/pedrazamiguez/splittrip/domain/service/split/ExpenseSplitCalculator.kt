package es.pedrazamiguez.splittrip.domain.service.split

import es.pedrazamiguez.splittrip.domain.model.ExpenseSplit

/**
 * Abstract base class for expense split calculation strategies.
 *
 * Uses the **Template Method** pattern:
 * [calculateShares] is the public entry point that enforces the
 * validate → execute contract. Subclasses implement the strategy-specific
 * [validate] and [executeCalculation] steps.
 */
abstract class ExpenseSplitCalculator {

    /**
     * Calculates the expense shares for the given participants.
     *
     * Validates inputs first, then delegates to the strategy-specific calculation.
     *
     * @param totalAmountCents The total expense amount in the smallest currency unit.
     * @param participantIds   The user IDs of all participants (non-excluded).
     * @param existingSplits   Pre-existing split data (used by EXACT and PERCENT strategies).
     * @return A list of [ExpenseSplit] representing each participant's share.
     */
    fun calculateShares(
        totalAmountCents: Long,
        participantIds: List<String>,
        existingSplits: List<ExpenseSplit> = emptyList()
    ): List<ExpenseSplit> {
        validate(totalAmountCents, participantIds, existingSplits)
        return executeCalculation(totalAmountCents, participantIds, existingSplits)
    }

    /**
     * Strategy-specific validation.
     * Throws [es.pedrazamiguez.splittrip.domain.exception.InvalidSplitException]
     * if the inputs are invalid for this strategy.
     */
    protected abstract fun validate(
        totalAmountCents: Long,
        participantIds: List<String>,
        existingSplits: List<ExpenseSplit>
    )

    /**
     * Strategy-specific calculation logic.
     */
    protected abstract fun executeCalculation(
        totalAmountCents: Long,
        participantIds: List<String>,
        existingSplits: List<ExpenseSplit>
    ): List<ExpenseSplit>
}
