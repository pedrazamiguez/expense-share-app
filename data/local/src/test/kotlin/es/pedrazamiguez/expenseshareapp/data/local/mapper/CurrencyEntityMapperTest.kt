package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.CurrencyEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExchangeRateEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRate
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import java.math.BigDecimal
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrencyEntityMapperTest {

    private val eurCurrency = Currency(code = "EUR", symbol = "€", defaultName = "Euro", decimalDigits = 2)
    private val eurEntity = CurrencyEntity(code = "EUR", symbol = "€", defaultName = "Euro", decimalDigits = 2)

    @Nested
    inner class ToDomainCurrency {

        @Test
        fun `maps CurrencyEntity to Currency`() {
            val result = CurrencyEntityMapper.toDomain(eurEntity)

            assertEquals("EUR", result.code)
            assertEquals("€", result.symbol)
            assertEquals("Euro", result.defaultName)
            assertEquals(2, result.decimalDigits)
        }
    }

    @Nested
    inner class ToEntityCurrency {

        @Test
        fun `maps Currency to CurrencyEntity`() {
            val result = CurrencyEntityMapper.toEntity(eurCurrency)

            assertEquals("EUR", result.code)
            assertEquals("€", result.symbol)
            assertEquals("Euro", result.defaultName)
            assertEquals(2, result.decimalDigits)
        }
    }

    @Nested
    inner class ToDomainExchangeRates {

        @Test
        fun `empty entity list returns EPOCH lastUpdated`() {
            val result = CurrencyEntityMapper.toDomain(emptyList(), eurCurrency)

            assertEquals(eurCurrency, result.baseCurrency)
            assertTrue(result.exchangeRates.isEmpty())
            assertEquals(Instant.EPOCH, result.lastUpdated)
        }

        @Test
        fun `non-empty entity list uses maxOf lastUpdated`() {
            val entities = listOf(
                ExchangeRateEntity(
                    baseCurrencyCode = "EUR",
                    currencyCode = "USD",
                    rate = BigDecimal("1.1"),
                    lastUpdated = 1000L
                ),
                ExchangeRateEntity(
                    baseCurrencyCode = "EUR",
                    currencyCode = "GBP",
                    rate = BigDecimal("0.85"),
                    lastUpdated = 2000L
                )
            )

            val result = CurrencyEntityMapper.toDomain(entities, eurCurrency)

            assertEquals(eurCurrency, result.baseCurrency)
            assertEquals(2, result.exchangeRates.size)
            assertEquals(Instant.ofEpochSecond(2000L), result.lastUpdated)
        }

        @Test
        fun `maps exchange rate currency codes`() {
            val entities = listOf(
                ExchangeRateEntity(
                    baseCurrencyCode = "EUR",
                    currencyCode = "USD",
                    rate = BigDecimal("1.1"),
                    lastUpdated = 1000L
                )
            )

            val result = CurrencyEntityMapper.toDomain(entities, eurCurrency)

            assertEquals("USD", result.exchangeRates[0].currency.code)
            assertEquals(0, BigDecimal("1.1").compareTo(result.exchangeRates[0].rate))
        }
    }

    @Nested
    inner class ToEntitiesExchangeRates {

        @Test
        fun `maps ExchangeRates to entity list`() {
            val usdCurrency = Currency(code = "USD", symbol = "$", defaultName = "US Dollar", decimalDigits = 2)
            val rates = ExchangeRates(
                baseCurrency = eurCurrency,
                exchangeRates = listOf(
                    ExchangeRate(currency = usdCurrency, rate = BigDecimal("1.1"))
                ),
                lastUpdated = Instant.ofEpochSecond(5000L)
            )

            val result = CurrencyEntityMapper.toEntities(rates)

            assertEquals(1, result.size)
            assertEquals("EUR", result[0].baseCurrencyCode)
            assertEquals("USD", result[0].currencyCode)
            assertEquals(0, BigDecimal("1.1").compareTo(result[0].rate))
            assertEquals(5000L, result[0].lastUpdated)
        }

        @Test
        fun `empty exchange rates maps to empty list`() {
            val rates = ExchangeRates(
                baseCurrency = eurCurrency,
                exchangeRates = emptyList(),
                lastUpdated = Instant.EPOCH
            )

            val result = CurrencyEntityMapper.toEntities(rates)

            assertTrue(result.isEmpty())
        }
    }
}
