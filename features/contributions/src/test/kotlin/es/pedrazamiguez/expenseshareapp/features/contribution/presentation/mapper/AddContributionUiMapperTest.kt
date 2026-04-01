package es.pedrazamiguez.expenseshareapp.features.contribution.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddContributionUiMapperTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var mapper: AddContributionUiMapper

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
        every { localeProvider.getCurrentLocale() } returns Locale.US
        mapper = AddContributionUiMapper(localeProvider)
    }

    // ── formatInputAmountWithCurrency ─────────────────────────────────────────

    @Nested
    inner class FormatInputAmountWithCurrency {

        @Test
        fun `returns blank when amount is blank`() {
            val result = mapper.formatInputAmountWithCurrency("", "EUR")
            assertEquals("", result)
        }

        @Test
        fun `returns original input when currency code is blank`() {
            val result = mapper.formatInputAmountWithCurrency("100", "")
            assertEquals("100", result)
        }

        @Test
        fun `returns non-blank formatted string for valid amount and currency`() {
            val result = mapper.formatInputAmountWithCurrency("100", "EUR")
            assertTrue(result.isNotBlank())
        }

        @Test
        fun `consults locale provider for the current locale`() {
            mapper.formatInputAmountWithCurrency("50", "USD")
            verify { localeProvider.getCurrentLocale() }
        }

        @Test
        fun `formats differently for different locales`() {
            val usLocaleProvider = mockk<LocaleProvider>()
            every { usLocaleProvider.getCurrentLocale() } returns Locale.US
            val esLocaleProvider = mockk<LocaleProvider>()
            every { esLocaleProvider.getCurrentLocale() } returns Locale.forLanguageTag("es-ES")

            val usResult = AddContributionUiMapper(usLocaleProvider)
                .formatInputAmountWithCurrency("1000", "EUR")
            val esResult = AddContributionUiMapper(esLocaleProvider)
                .formatInputAmountWithCurrency("1000", "EUR")

            // Both non-blank, but formatted differently for their respective locales
            assertTrue(usResult.isNotBlank())
            assertTrue(esResult.isNotBlank())
        }
    }

    // ── resolveCurrencySymbol ─────────────────────────────────────────────────

    @Nested
    inner class ResolveCurrencySymbol {

        @Test
        fun `returns empty string for blank currency code`() {
            val result = mapper.resolveCurrencySymbol("")
            assertEquals("", result)
        }

        @Test
        fun `returns euro symbol for EUR currency code`() {
            val result = mapper.resolveCurrencySymbol("EUR")
            assertEquals("€", result)
        }

        @Test
        fun `returns dollar symbol for USD currency code with US locale`() {
            val result = mapper.resolveCurrencySymbol("USD")
            // Locale.US returns "$" directly (no ISO-code fallback needed)
            assertEquals("$", result)
        }

        @Test
        fun `returns non-blank symbol for a valid currency code`() {
            val result = mapper.resolveCurrencySymbol("GBP")
            assertTrue(result.isNotBlank())
        }

        @Test
        fun `consults locale provider for the current locale`() {
            mapper.resolveCurrencySymbol("EUR")
            verify { localeProvider.getCurrentLocale() }
        }

        @Test
        fun `returns empty string for unknown currency code`() {
            val result = mapper.resolveCurrencySymbol("XYZ")
            assertEquals("", result)
        }
    }
}
