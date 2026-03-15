package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpenseSplitCalculatorFactoryTest {

    private lateinit var factory: ExpenseSplitCalculatorFactory

    @BeforeEach
    fun setUp() {
        factory = ExpenseSplitCalculatorFactory(ExpenseCalculatorService())
    }

    @Test
    fun `creates EqualSplitCalculator for EQUAL type`() {
        val calculator = factory.create(SplitType.EQUAL)
        assertTrue(calculator is EqualSplitCalculator)
    }

    @Test
    fun `creates ExactSplitCalculator for EXACT type`() {
        val calculator = factory.create(SplitType.EXACT)
        assertTrue(calculator is ExactSplitCalculator)
    }

    @Test
    fun `creates PercentSplitCalculator for PERCENT type`() {
        val calculator = factory.create(SplitType.PERCENT)
        assertTrue(calculator is PercentSplitCalculator)
    }
}
