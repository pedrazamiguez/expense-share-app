package es.pedrazamiguez.expenseshareapp.data.remote.mapper

import es.pedrazamiguez.expenseshareapp.data.remote.dto.ExchangeRateResponse
import java.math.BigDecimal
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrencyDtoMapperTest {

    @Nested
    inner class MapCurrencies {

        @Test
        fun `maps known symbol USD to dollar sign`() {
            val response = mapOf("USD" to "US Dollar")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(1, result.size)
            assertEquals("USD", result[0].code)
            assertEquals("$", result[0].symbol)
            assertEquals("US Dollar", result[0].defaultName)
        }

        @Test
        fun `maps known symbol EUR to euro sign`() {
            val response = mapOf("EUR" to "Euro")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals("€", result[0].symbol)
        }

        @Test
        fun `unknown currency code falls back to code as symbol`() {
            val response = mapOf("XYZ" to "Unknown Currency")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals("XYZ", result[0].symbol)
            assertEquals("Unknown Currency", result[0].defaultName)
        }

        @Test
        fun `zero-decimal currency JPY has 0 decimal digits`() {
            val response = mapOf("JPY" to "Japanese Yen")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(0, result[0].decimalDigits)
        }

        @Test
        fun `zero-decimal currency KRW has 0 decimal digits`() {
            val response = mapOf("KRW" to "South Korean Won")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(0, result[0].decimalDigits)
        }

        @Test
        fun `three-decimal currency BHD has 3 decimal digits`() {
            val response = mapOf("BHD" to "Bahraini Dinar")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(3, result[0].decimalDigits)
        }

        @Test
        fun `three-decimal currency KWD has 3 decimal digits`() {
            val response = mapOf("KWD" to "Kuwaiti Dinar")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(3, result[0].decimalDigits)
        }

        @Test
        fun `standard currency has 2 decimal digits`() {
            val response = mapOf("GBP" to "British Pound")

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(2, result[0].decimalDigits)
        }

        @Test
        fun `maps multiple currencies`() {
            val response = mapOf(
                "USD" to "US Dollar",
                "EUR" to "Euro",
                "GBP" to "British Pound"
            )

            val result = CurrencyDtoMapper.mapCurrencies(response)

            assertEquals(3, result.size)
        }

        @Test
        fun `empty map returns empty list`() {
            val result = CurrencyDtoMapper.mapCurrencies(emptyMap())
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class MapExchangeRates {

        @Test
        fun `maps base currency correctly`() {
            val response = ExchangeRateResponse(
                disclaimer = null,
                license = null,
                timestamp = 1000L,
                base = "USD",
                rates = mapOf("EUR" to BigDecimal("0.92"))
            )

            val result = CurrencyDtoMapper.mapExchangeRates(response)

            assertEquals("USD", result.baseCurrency.code)
            assertEquals("$", result.baseCurrency.symbol)
        }

        @Test
        fun `maps timestamp to Instant`() {
            val response = ExchangeRateResponse(
                disclaimer = null,
                license = null,
                timestamp = 1711800000L,
                base = "USD",
                rates = emptyMap()
            )

            val result = CurrencyDtoMapper.mapExchangeRates(response)

            assertEquals(Instant.ofEpochSecond(1711800000L), result.lastUpdated)
        }

        @Test
        fun `maps rates with currency codes and symbols`() {
            val response = ExchangeRateResponse(
                disclaimer = null,
                license = null,
                timestamp = 1000L,
                base = "USD",
                rates = mapOf(
                    "EUR" to BigDecimal("0.92"),
                    "GBP" to BigDecimal("0.79")
                )
            )

            val result = CurrencyDtoMapper.mapExchangeRates(response)

            assertEquals(2, result.exchangeRates.size)
            val eurRate = result.exchangeRates.first { it.currency.code == "EUR" }
            assertEquals("€", eurRate.currency.symbol)
            assertEquals(0, BigDecimal("0.92").compareTo(eurRate.rate))
        }

        @Test
        fun `empty rates returns empty exchange rate list`() {
            val response = ExchangeRateResponse(
                disclaimer = null,
                license = null,
                timestamp = 1000L,
                base = "USD",
                rates = emptyMap()
            )

            val result = CurrencyDtoMapper.mapExchangeRates(response)

            assertTrue(result.exchangeRates.isEmpty())
        }

        @Test
        fun `unknown base currency uses code as symbol`() {
            val response = ExchangeRateResponse(
                disclaimer = null,
                license = null,
                timestamp = 1000L,
                base = "XYZ",
                rates = emptyMap()
            )

            val result = CurrencyDtoMapper.mapExchangeRates(response)

            assertEquals("XYZ", result.baseCurrency.code)
            assertEquals("XYZ", result.baseCurrency.symbol)
        }
    }
}
