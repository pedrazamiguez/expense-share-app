package es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DateFormatter and FormattingHelper")
class DateFormatterTest {

    private val usLocale = Locale.US
    private val esLocale = Locale.forLanguageTag("es-ES")

    private val testLocalDateTime = LocalDateTime.of(2026, 6, 15, 14, 30)
    private val testLocalDate = LocalDate.of(2026, 6, 15)

    @Nested
    @DisplayName("LocalDateTime formatting")
    inner class LocalDateTimeFormatting {

        @Test
        fun `formatShortDate formats correctly with US locale`() {
            val result = testLocalDateTime.formatShortDate(usLocale)
            assertEquals("15 Jun", result)
        }

        @Test
        fun `formatShortDate formats correctly with Spanish locale`() {
            val result = testLocalDateTime.formatShortDate(esLocale)
            assertEquals("15 jun", result.lowercase())
        }

        @Test
        fun `formatMediumDate formats correctly with US locale`() {
            val result = testLocalDateTime.formatMediumDate(usLocale)
            assertEquals("June 2026", result)
        }

        @Test
        fun `formatMediumDate formats correctly with Spanish locale`() {
            val result = testLocalDateTime.formatMediumDate(esLocale)
            assertEquals("junio 2026", result.lowercase())
        }
    }

    @Nested
    @DisplayName("LocalDate formatting")
    inner class LocalDateFormatting {

        @Test
        fun `formatShortDate formats correctly with US locale`() {
            val result = testLocalDate.formatShortDate(usLocale)
            assertEquals("15 Jun", result)
        }

        @Test
        fun `formatShortDate formats correctly with Spanish locale`() {
            val result = testLocalDate.formatShortDate(esLocale)
            assertEquals("15 jun", result.lowercase())
        }

        @Test
        fun `formatMediumDate formats correctly with US locale`() {
            val result = testLocalDate.formatMediumDate(usLocale)
            assertEquals("June 2026", result)
        }

        @Test
        fun `formatMediumDate formats correctly with Spanish locale`() {
            val result = testLocalDate.formatMediumDate(esLocale)
            assertEquals("junio 2026", result.lowercase())
        }
    }

    @Nested
    @DisplayName("FormattingHelper date delegation")
    inner class FormattingHelperDateDelegation {

        private val localeProvider: LocaleProvider = mockk {
            every { getCurrentLocale() } returns usLocale
        }
        private val helper = FormattingHelper(localeProvider = localeProvider)

        @Test
        fun `formatShortDate with LocalDateTime formats or returns empty on null`() {
            assertEquals("15 Jun", helper.formatShortDate(testLocalDateTime))
            assertEquals("", helper.formatShortDate(null as LocalDateTime?))
        }

        @Test
        fun `formatShortDate with LocalDate formats or returns empty on null`() {
            assertEquals("15 Jun", helper.formatShortDate(testLocalDate))
            assertEquals("", helper.formatShortDate(null as LocalDate?))
        }

        @Test
        fun `formatMediumDate with LocalDate formats or returns empty on null`() {
            assertEquals("June 2026", helper.formatMediumDate(testLocalDate))
            assertEquals("", helper.formatMediumDate(null as LocalDate?))
        }
    }
}
