package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.exception.InvalidSplitException
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit

/**
 * Each participant's share is specified as a fixed amount in cents.
 *
 * Validation: the individual shares must sum exactly to [totalAmountCents].
 */
class ExactSplitCalculator : ExpenseSplitCalculator() {

    override fun validate(
        totalAmountCents: Long,
        participantIds: List<String>,
        existingSplits: List<ExpenseSplit>
    ) {
        if (participantIds.isEmpty()) {
            throw InvalidSplitException(
                splitType = SplitType.EXACT,
                message = "At least one participant is required for exact split"
            )
        }

        val splitSum = existingSplits
            .filter { it.userId in participantIds && !it.isExcluded }
            .sumOf { it.amountCents }

        if (splitSum != totalAmountCents) {
            throw InvalidSplitException(
                splitType = SplitType.EXACT,
                message = "Exact split amounts must sum to $totalAmountCents, but got $splitSum"
            )
        }
    }

    override fun executeCalculation(
        totalAmountCents: Long,
        participantIds: List<String>,
        existingSplits: List<ExpenseSplit>
    ): List<ExpenseSplit> {
        // For exact splits, the amounts are already specified by the user.
        // Return the existing splits filtered to active participants.
        return existingSplits.filter { it.userId in participantIds && !it.isExcluded }
    }
}

