package es.pedrazamiguez.expenseshareapp.domain.converter

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

class CurrencyConverterTest {

    private val usd = Currency("USD", "$", "US Dollar", 2)
    private val eur = Currency("EUR", "€", "Euro", 2)
    private val mxn = Currency("MXN", "$", "Mexican Peso", 2)

    private val rates = ExchangeRates(
        baseCurrency = usd, rates = listOf(
            ExchangeRates.Rate(eur, BigDecimal("0.9")), // 1 USD =  0.9 EUR
            ExchangeRates.Rate(mxn, BigDecimal("20.0")) // 1 USD = 20.0 MXN
        ), lastUpdated = Instant.now()
    )

    @Test
    fun `convert USD to EUR`() = runTest {
        val result = CurrencyConverter.convert(BigDecimal("10.00"), usd, eur, rates)
        assertEquals(BigDecimal("9.00"), result)
    }

    @Test
    fun `convert EUR to USD`() = runTest {
        val result = CurrencyConverter.convert(BigDecimal("9.00"), eur, usd, rates)
        assertEquals(BigDecimal("10.00"), result)
    }

    @Test
    fun `convert EUR to MXN`() = runTest {
        val result = CurrencyConverter.convert(BigDecimal("9.00"), eur, mxn, rates)
        assertEquals(BigDecimal("200.00"), result)
    }

    @Test
    fun `throws if missing rate`() = runTest {
        val inr = Currency("INR", "₹", "Indian Rupee", 2)
        assertThrows<IllegalArgumentException> {
            CurrencyConverter.convert(BigDecimal("10.00"), inr, eur, rates)
        }
    }

}
