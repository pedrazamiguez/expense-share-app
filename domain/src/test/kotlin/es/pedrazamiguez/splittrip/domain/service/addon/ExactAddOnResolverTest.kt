package es.pedrazamiguez.splittrip.domain.service.addon

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExactAddOnResolverTest {

    private val resolver = ExactAddOnResolver()

    @Test
    fun `converts major units to cents with 2 decimal places`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("5.50"),
            decimalDigits = 2,
            sourceAmountCents = 0L // not used by EXACT
        )
        assertEquals(550L, result)
    }

    @Test
    fun `converts whole number input to cents`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("10"),
            decimalDigits = 2,
            sourceAmountCents = 0L
        )
        assertEquals(1000L, result)
    }

    @Test
    fun `handles zero-decimal currencies like JPY`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("500"),
            decimalDigits = 0,
            sourceAmountCents = 0L
        )
        assertEquals(500L, result)
    }

    @Test
    fun `handles 3-decimal currencies like TND`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("1.234"),
            decimalDigits = 3,
            sourceAmountCents = 0L
        )
        assertEquals(1234L, result)
    }

    @Test
    fun `rounds HALF_UP when input has more decimals than currency`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("1.555"),
            decimalDigits = 2,
            sourceAmountCents = 0L
        )
        assertEquals(156L, result) // 1.555 * 100 = 155.5 → rounds to 156
    }

    @Test
    fun `returns zero for zero input`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal.ZERO,
            decimalDigits = 2,
            sourceAmountCents = 10000L
        )
        assertEquals(0L, result)
    }

    @Test
    fun `ignores sourceAmountCents entirely`() {
        val result = resolver.resolve(
            normalizedInput = BigDecimal("3.00"),
            decimalDigits = 2,
            sourceAmountCents = 99999L
        )
        assertEquals(300L, result)
    }
}
