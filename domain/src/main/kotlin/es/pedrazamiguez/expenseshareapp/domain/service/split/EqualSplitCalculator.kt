package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.exception.InvalidSplitException
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import java.math.BigDecimal

/**
 * Splits an expense equally among all participants.
 *
 * Reuses [ExpenseCalculatorService.distributeAmount] for fair remainder distribution
 * (e.g., 10.00 / 3 → [3.34, 3.33, 3.33]).
 */
class EqualSplitCalculator(private val expenseCalculatorService: ExpenseCalculatorService) : ExpenseSplitCalculator() {

    override fun validate(totalAmountCents: Long, participantIds: List<String>, existingSplits: List<ExpenseSplit>) {
        if (participantIds.isEmpty()) {
            throw InvalidSplitException(
                splitType = SplitType.EQUAL,
                message = "At least one non-excluded participant is required for equal split"
            )
        }
    }

    override fun executeCalculation(
        totalAmountCents: Long,
        participantIds: List<String>,
        existingSplits: List<ExpenseSplit>
    ): List<ExpenseSplit> {
        // distributeAmount works with BigDecimal amounts (in major units with 2 decimal places)
        // but we work with cents (Long). Convert: cents → BigDecimal with scale 2, then back.
        val totalAmount = BigDecimal(totalAmountCents).movePointLeft(2)
        val shares = expenseCalculatorService.distributeAmount(totalAmount, participantIds.size)

        return participantIds.mapIndexed { index, userId ->
            val shareCents = shares[index].movePointRight(2).toLong()
            ExpenseSplit(
                userId = userId,
                amountCents = shareCents
            )
        }
    }
}
