package es.pedrazamiguez.expenseshareapp.domain.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ExpenseCalculatorServiceTest {

    private val service = ExpenseCalculatorService()

    // BigDecimal-based method tests
    @Test
    fun `calculateGroupAmount multiplies source by rate`() {
        val result = service.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal("0.027")
        )
        assertEquals(BigDecimal("2.70"), result)
    }

    @Test
    fun `calculateGroupAmount returns zero when rate is zero`() {
        val result = service.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal.ZERO
        )
        assertEquals(BigDecimal.ZERO, result)
    }

    @Test
    fun `calculateImpliedRate divides target by source`() {
        val result = service.calculateImpliedRate(
            sourceAmount = BigDecimal("1000.00"),
            groupAmount = BigDecimal("27.35")
        )
        assertEquals(BigDecimal("0.027350"), result)
    }

    @Test
    fun `calculateImpliedRate returns zero when source is zero`() {
        val result = service.calculateImpliedRate(
            sourceAmount = BigDecimal.ZERO,
            groupAmount = BigDecimal("27.35")
        )
        assertEquals(BigDecimal.ZERO, result)
    }

    // String-based method tests
    @Test
    fun `calculateGroupAmountFromStrings handles valid inputs`() {
        val result = service.calculateGroupAmountFromStrings(
            sourceAmountString = "100.00",
            exchangeRateString = "1.5"
        )
        assertEquals("150.00", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles empty source amount`() {
        val result = service.calculateGroupAmountFromStrings(
            sourceAmountString = "",
            exchangeRateString = "1.5"
        )
        assertEquals("0.00", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles invalid rate defaults to one`() {
        val result = service.calculateGroupAmountFromStrings(
            sourceAmountString = "100.00",
            exchangeRateString = "invalid"
        )
        assertEquals("100.00", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles valid inputs`() {
        val result = service.calculateImpliedRateFromStrings(
            sourceAmountString = "1000.00",
            groupAmountString = "27.35"
        )
        assertEquals("0.02735", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles empty source amount`() {
        val result = service.calculateImpliedRateFromStrings(
            sourceAmountString = "",
            groupAmountString = "27.35"
        )
        assertEquals("0", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings handles invalid group amount`() {
        val result = service.calculateImpliedRateFromStrings(
            sourceAmountString = "100.00",
            groupAmountString = "invalid"
        )
        assertEquals("0", result)
    }

    // centsToBigDecimal tests
    @Test
    fun `centsToBigDecimal converts cents to decimal for standard currency`() {
        val result = service.centsToBigDecimal(12345L)
        assertEquals(BigDecimal("123.45"), result)
    }

    @Test
    fun `centsToBigDecimal handles zero decimal places currency like JPY`() {
        val result = service.centsToBigDecimal(12345L, decimalPlaces = 0)
        assertEquals(BigDecimal("12345"), result)
    }

    @Test
    fun `centsToBigDecimal handles three decimal places currency like TND`() {
        val result = service.centsToBigDecimal(12345L, decimalPlaces = 3)
        assertEquals(BigDecimal("12.345"), result)
    }

    // Variable decimal places tests
    @Test
    fun `calculateGroupAmount respects target decimal places for JPY`() {
        val result = service.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal("157.25"),
            targetDecimalPlaces = 0
        )
        assertEquals(BigDecimal("15725"), result)
    }

    @Test
    fun `calculateGroupAmount respects target decimal places for TND`() {
        val result = service.calculateGroupAmount(
            sourceAmount = BigDecimal("100.00"),
            rate = BigDecimal("3.12345"),
            targetDecimalPlaces = 3
        )
        assertEquals(BigDecimal("312.345"), result)
    }

    @Test
    fun `calculateGroupAmountFromStrings respects source and target decimal places`() {
        // Converting from JPY (0 decimals) to EUR (2 decimals)
        val result = service.calculateGroupAmountFromStrings(
            sourceAmountString = "15725",
            exchangeRateString = "0.00636",
            sourceDecimalPlaces = 0,
            targetDecimalPlaces = 2
        )
        assertEquals("100.01", result)
    }

    @Test
    fun `calculateGroupAmountFromStrings handles TND to EUR conversion`() {
        // Converting from TND (3 decimals) to EUR (2 decimals)
        val result = service.calculateGroupAmountFromStrings(
            sourceAmountString = "312.345",
            exchangeRateString = "0.32",
            sourceDecimalPlaces = 3,
            targetDecimalPlaces = 2
        )
        assertEquals("99.95", result)
    }

    @Test
    fun `calculateImpliedRateFromStrings respects source decimal places`() {
        // Source is JPY (0 decimals)
        val result = service.calculateImpliedRateFromStrings(
            sourceAmountString = "15725",
            groupAmountString = "100.00",
            sourceDecimalPlaces = 0
        )
        assertEquals("0.006359", result)
    }
}
