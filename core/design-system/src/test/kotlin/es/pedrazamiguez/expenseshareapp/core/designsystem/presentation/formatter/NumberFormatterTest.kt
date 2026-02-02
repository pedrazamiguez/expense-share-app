package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Locale

@DisplayName("NumberFormatter")
class NumberFormatterTest {

    @Nested
    @DisplayName("String.formatNumberForDisplay()")
    inner class FormatNumberForDisplay {

        @Test
        fun `formats number with US locale using dot as decimal separator`() {
            // Given
            val number = "1234.56"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1,234.56", result)
        }

        @Test
        fun `formats number with Spanish locale using comma as decimal separator`() {
            // Given
            val number = "1234.56"
            val locale = Locale.forLanguageTag("es-ES")

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1.234,56", result)
        }

        @Test
        fun `formats number with French locale using space as grouping separator`() {
            // Given
            val number = "1234.56"
            val locale = Locale.FRANCE

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1\u202f234,56", result)
        }

        @Test
        fun `formats zero correctly`() {
            // Given
            val number = "0"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("0", result)
        }

        @Test
        fun `formats zero with decimals correctly`() {
            // Given
            val number = "0.00"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("0", result)
        }

        @Test
        fun `formats very large numbers correctly`() {
            // Given
            val number = "9999999999.99"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("9,999,999,999.99", result)
        }

        @Test
        fun `formats very small decimals correctly`() {
            // Given
            val number = "0.000001"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 6)

            // Then
            assertEquals("0.000001", result)
        }

        @Test
        fun `truncates trailing zeros`() {
            // Given
            val number = "123.450000"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 6)

            // Then
            assertEquals("123.45", result)
        }

        @Test
        fun `respects maxDecimalPlaces limit`() {
            // Given
            val number = "123.456789"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("123.46", result) // Rounded up
        }

        @Test
        fun `formats with 6 decimal places for exchange rates`() {
            // Given
            val number = "37.223456"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 6)

            // Then
            assertEquals("37.223456", result)
        }

        @Test
        fun `handles invalid number string gracefully`() {
            // Given
            val invalidNumber = "not-a-number"
            val locale = Locale.US

            // When
            val result = invalidNumber.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("not-a-number", result) // Returns original string
        }

        @Test
        fun `handles empty string gracefully`() {
            // Given
            val emptyString = ""
            val locale = Locale.US

            // When
            val result = emptyString.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("", result)
        }

        @Test
        fun `formats negative numbers correctly`() {
            // Given
            val number = "-1234.56"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("-1,234.56", result)
        }

        @Test
        fun `formats number with zero decimal places`() {
            // Given
            val number = "1234.56"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 0)

            // Then
            assertEquals("1,235", result) // Rounded
        }

        @Test
        fun `formats whole numbers without decimal separator`() {
            // Given
            val number = "1234"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1,234", result)
        }

        @Test
        fun `formats number with minDecimalPlaces padding zeros - EUR style`() {
            // Given: 1.1 should display as 1.10 for EUR (2 decimal places required)
            val number = "1.1"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 2,
                minDecimalPlaces = 2
            )

            // Then
            assertEquals("1.10", result)
        }

        @Test
        fun `formats whole number with minDecimalPlaces padding zeros`() {
            // Given: 100 should display as 100.00 for EUR
            val number = "100"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 2,
                minDecimalPlaces = 2
            )

            // Then
            assertEquals("100.00", result)
        }

        @Test
        fun `formats number with minDecimalPlaces in Spanish locale`() {
            // Given: 1.1 should display as 1,10 for EUR in Spanish
            val number = "1.1"
            val locale = Locale.forLanguageTag("es-ES")

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 2,
                minDecimalPlaces = 2
            )

            // Then
            assertEquals("1,10", result)
        }

        @Test
        fun `formats zero with minDecimalPlaces padding`() {
            // Given: 0 should display as 0.00 for EUR
            val number = "0"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 2,
                minDecimalPlaces = 2
            )

            // Then
            assertEquals("0.00", result)
        }

        @Test
        fun `formats JPY with zero decimal places`() {
            // Given: JPY has 0 decimal places
            val number = "1234.56"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 0,
                minDecimalPlaces = 0
            )

            // Then
            assertEquals("1,235", result) // Rounded
        }

        @Test
        fun `minDecimalPlaces is capped at maxDecimalPlaces`() {
            // Given: minDecimalPlaces > maxDecimalPlaces should be capped
            val number = "1.1"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 2,
                minDecimalPlaces = 5 // Higher than max, should be capped to 2
            )

            // Then
            assertEquals("1.10", result)
        }

        @Test
        fun `formats number already having required decimals unchanged`() {
            // Given: 1.10 already has 2 decimals
            val number = "1.10"
            val locale = Locale.US

            // When
            val result = number.formatNumberForDisplay(
                locale,
                maxDecimalPlaces = 2,
                minDecimalPlaces = 2
            )

            // Then
            assertEquals("1.10", result)
        }
    }

    @Nested
    @DisplayName("String.formatRateForDisplay()")
    inner class FormatRateForDisplay {

        @Test
        fun `formats exchange rate with US locale`() {
            // Given
            val rate = "37.223456"
            val locale = Locale.US

            // When
            val result = rate.formatRateForDisplay(locale)

            // Then
            assertEquals("37.223456", result)
        }

        @Test
        fun `formats exchange rate with Spanish locale`() {
            // Given
            val rate = "37.223456"
            val locale = Locale.forLanguageTag("es-ES")

            // When
            val result = rate.formatRateForDisplay(locale)

            // Then
            assertEquals("37,223456", result)
        }

        @Test
        fun `uses default 6 decimal places for rates`() {
            // Given
            val rate = "1.123456789"
            val locale = Locale.US

            // When
            val result = rate.formatRateForDisplay(locale)

            // Then
            assertEquals("1.123457", result) // Rounded to 6 decimal places
        }

        @Test
        fun `formats rate of 1 correctly`() {
            // Given
            val rate = "1"
            val locale = Locale.US

            // When
            val result = rate.formatRateForDisplay(locale)

            // Then
            assertEquals("1", result)
        }

        @Test
        fun `handles very small exchange rates`() {
            // Given
            val rate = "0.000123"
            val locale = Locale.US

            // When
            val result = rate.formatRateForDisplay(locale)

            // Then
            assertEquals("0.000123", result)
        }

        @Test
        fun `handles very large exchange rates`() {
            // Given
            val rate = "100000.123456"
            val locale = Locale.US

            // When
            val result = rate.formatRateForDisplay(locale)

            // Then
            assertEquals("100,000.123456", result)
        }
    }

    @Nested
    @DisplayName("BigDecimal.formatForDisplay()")
    inner class BigDecimalFormatForDisplay {

        @Test
        fun `formats BigDecimal with US locale`() {
            // Given
            val number = BigDecimal("1234.56")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1,234.56", result)
        }

        @Test
        fun `formats BigDecimal with Spanish locale`() {
            // Given
            val number = BigDecimal("1234.56")
            val locale = Locale.forLanguageTag("es-ES")

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1.234,56", result)
        }

        @Test
        fun `formats BigDecimal zero`() {
            // Given
            val number = BigDecimal.ZERO
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("0", result)
        }

        @Test
        fun `formats very large BigDecimal`() {
            // Given
            val number = BigDecimal("9999999999999.99")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("9,999,999,999,999.99", result)
        }

        @Test
        fun `formats very small BigDecimal`() {
            // Given
            val number = BigDecimal("0.0000000001")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 10)

            // Then
            assertEquals("0.0000000001", result)
        }

        @Test
        fun `formats negative BigDecimal`() {
            // Given
            val number = BigDecimal("-1234.56")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("-1,234.56", result)
        }

        @Test
        fun `uses default 2 decimal places when not specified`() {
            // Given
            val number = BigDecimal("123.456789")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale)

            // Then
            assertEquals("123.46", result) // Default is 2 decimal places, rounded
        }

        @Test
        fun `truncates trailing zeros in BigDecimal`() {
            // Given
            val number = BigDecimal("100.00")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("100", result)
        }

        @Test
        fun `formats BigDecimal with scientific notation input`() {
            // Given
            val number = BigDecimal("1.23E+3") // 1230
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("1,230", result)
        }

        @Test
        fun `formats BigDecimal with high precision`() {
            // Given
            val number = BigDecimal("12345678.123456789")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 6)

            // Then
            assertEquals("12,345,678.123457", result) // Rounded to 6 places
        }
    }

    @Nested
    @DisplayName("Cross-Locale Consistency")
    inner class CrossLocaleConsistency {

        @Test
        fun `formats same number differently across locales`() {
            // Given
            val number = "12345.67"

            // When
            val usResult = number.formatNumberForDisplay(Locale.US, maxDecimalPlaces = 2)
            val esResult = number.formatNumberForDisplay(Locale.forLanguageTag("es-ES"), maxDecimalPlaces = 2)
            val frResult = number.formatNumberForDisplay(Locale.FRANCE, maxDecimalPlaces = 2)

            // Then
            assertEquals("12,345.67", usResult)
            assertEquals("12.345,67", esResult)
            // French uses narrow no-break space (U+202F) as grouping separator on newer JVMs
            assertEquals("12\u202f345,67", frResult)
        }

        @Test
        fun `exchange rate formatting is consistent across locales`() {
            // Given
            val rate = "0.025678"

            // When
            val usResult = rate.formatRateForDisplay(Locale.US)
            val esResult = rate.formatRateForDisplay(Locale.forLanguageTag("es-ES"))

            // Then
            assertEquals("0.025678", usResult)
            assertEquals("0,025678", esResult)
        }

        @Test
        fun `BigDecimal formatting matches String formatting for same input`() {
            // Given
            val stringNumber = "1234.56"
            val bigDecimalNumber = BigDecimal(stringNumber)
            val locale = Locale.US

            // When
            val stringResult = stringNumber.formatNumberForDisplay(locale, maxDecimalPlaces = 2)
            val bigDecimalResult = bigDecimalNumber.formatForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals(stringResult, bigDecimalResult)
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCases {

        @Test
        fun `handles null-like string values gracefully`() {
            // Given
            val nullString = "null"
            val locale = Locale.US

            // When
            val result = nullString.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("null", result) // Returns as-is since it's not a number
        }

        @Test
        fun `handles whitespace-only string`() {
            // Given
            val whitespace = "   "
            val locale = Locale.US

            // When
            val result = whitespace.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("   ", result)
        }

        @Test
        fun `handles number with multiple decimal points`() {
            // Given
            val invalidNumber = "123.45.67"
            val locale = Locale.US

            // When
            val result = invalidNumber.formatNumberForDisplay(locale, maxDecimalPlaces = 2)

            // Then
            assertEquals("123.45.67", result) // Returns as-is since parsing fails
        }

        @Test
        fun `handles extremely precise decimal`() {
            // Given
            val preciseNumber = "1.123456789012345678901234567890"
            val locale = Locale.US

            // When
            val result = preciseNumber.formatNumberForDisplay(locale, maxDecimalPlaces = 6)

            // Then
            assertEquals("1.123457", result) // Rounded to 6 decimal places
        }

        @Test
        fun `formats with zero maxDecimalPlaces shows integer only`() {
            // Given
            val number = BigDecimal("999.999")
            val locale = Locale.US

            // When
            val result = number.formatForDisplay(locale, maxDecimalPlaces = 0)

            // Then
            assertEquals("1,000", result) // Rounded up
        }
    }
}
