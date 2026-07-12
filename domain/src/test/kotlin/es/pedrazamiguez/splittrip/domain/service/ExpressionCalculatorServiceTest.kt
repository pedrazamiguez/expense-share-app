package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionResult
import es.pedrazamiguez.splittrip.domain.service.calculator.impl.ExpressionCalculatorServiceImpl
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpressionCalculatorServiceTest {

    private lateinit var evaluator: ExpressionCalculatorServiceImpl

    @BeforeEach
    fun setUp() {
        evaluator = ExpressionCalculatorServiceImpl()
    }

    @Test
    fun `evaluate returns empty when input is blank`() {
        assertEquals(ExpressionResult.Failure.Empty, evaluator.evaluate(""))
        assertEquals(ExpressionResult.Failure.Empty, evaluator.evaluate("   "))
    }

    @Test
    fun `evaluate single number`() {
        val result = evaluator.evaluate("17.85")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("17.85").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate locale aware single number`() {
        val result = evaluator.evaluate("17,85")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("17.85").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate simple addition`() {
        val result = evaluator.evaluate("10 + 20.5")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("30.5").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate subtraction with negative result`() {
        val result = evaluator.evaluate("10 − 20")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("-10").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate multiplication`() {
        val result = evaluator.evaluate("17.85 × 3")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("53.55").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate multiplication with negative number`() {
        val result = evaluator.evaluate("-17.85 × 3")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("-53.55").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate division`() {
        val result = evaluator.evaluate("100 ÷ 3")
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("33.3333333333").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate operator precedence`() {
        val result = evaluator.evaluate("100 ÷ 4 + 5 × 2")
        // 100 / 4 + 5 * 2 = 25 + 10 = 35
        assertTrue(result is ExpressionResult.Success)
        assertEquals(0, BigDecimal("35").compareTo((result as ExpressionResult.Success).value))
    }

    @Test
    fun `evaluate DivisionByZero`() {
        val result = evaluator.evaluate("10 ÷ 0")
        assertEquals(ExpressionResult.Failure.DivisionByZero, result)
    }

    @Test
    fun `evaluate Malformed expression`() {
        val result = evaluator.evaluate("4+2×")
        assertEquals(ExpressionResult.Failure.Malformed, result)
    }

    @Test
    fun `evaluate Malformed with two operators`() {
        val result = evaluator.evaluate("4++2")
        assertEquals(ExpressionResult.Failure.Malformed, result)
    }
}
