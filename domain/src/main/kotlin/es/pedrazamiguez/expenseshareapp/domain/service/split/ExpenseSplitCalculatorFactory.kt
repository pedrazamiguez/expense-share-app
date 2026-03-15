package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService

/**
 * Factory that vends the correct [ExpenseSplitCalculator] strategy based on [SplitType].
 *
 * Uses constructor injection for dependencies required by specific strategies.
 */
class ExpenseSplitCalculatorFactory(private val expenseCalculatorService: ExpenseCalculatorService) {

    fun create(splitType: SplitType): ExpenseSplitCalculator = when (splitType) {
        SplitType.EQUAL -> EqualSplitCalculator(expenseCalculatorService)
        SplitType.EXACT -> ExactSplitCalculator()
        SplitType.PERCENT -> PercentSplitCalculator()
    }
}
