package es.pedrazamiguez.expenseshareapp.domain.service.addon

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PercentageAddOnResolverTest {

    private val resolver = PercentageAddOnResolver()

    @Test
    fun `10 percent of 10000 cents equals 1000 cents`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("10"),
            decimalDigits = 2,
            sourceAmountCents = 10000L
        )
        assertEquals(1000L, result)
    }

    @Test
    fun `50 percent of 5000 cents equals 2500 cents`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("50"),
            decimalDigits = 2,
            sourceAmountCents = 5000L
        )
        assertEquals(2500L, result)
    }

    @Test
    fun `100 percent returns the full source amount`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("100"),
            decimalDigits = 2,
            sourceAmountCents = 7500L
        )
        assertEquals(7500L, result)
    }

    @Test
    fun `fractional percent rounds HALF_UP`() {
        // 15% of 333 cents = 49.95 → rounds to 50
        val result = resolver.resolve(
            normalizedInput = BigDecimal("15"),
            decimalDigits = 2,
            sourceAmountCents = 333L
        )
        assertEquals(50L, result)
    }

    @Test
    fun `decimal percentage like 7_5 percent`() {
        // 7.5% of 10000 = 750
        val result = resolver.resolve(
            normalizedInput = BigDecimal("7.5"),
            decimalDigits = 2,
            sourceAmountCents = 10000L
        )
        assertEquals(750L, result)
    }

    @Test
    fun `returns zero when sourceAmountCents is zero`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("10"),
            decimalDigits = 2,
            sourceAmountCents = 0L
        )
        assertEquals(0L, result)
    }

    @Test
    fun `returns zero when sourceAmountCents is negative`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("10"),
            decimalDigits = 2,
            sourceAmountCents = -100L
        )
        assertEquals(0L, result)
    }

    @Test
    fun `returns zero when percentage input is zero`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal.ZERO,
            decimalDigits = 2,
            sourceAmountCents = 10000L
        )
        assertEquals(0L, result)
    }
}
