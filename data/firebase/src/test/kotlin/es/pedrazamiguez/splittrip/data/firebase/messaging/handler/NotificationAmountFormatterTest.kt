package es.pedrazamiguez.splittrip.data.firebase.messaging.handler

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NotificationAmountFormatter")
class NotificationAmountFormatterTest {

    private lateinit var localeProvider: LocaleProvider

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
    }

    // ---------- \u2800 substitution (prevents currency symbol detachment) ----------

    @Nested
    @DisplayName("Braille blank substitution for notification line-break safety")
    inner class BrailleBlankSubstitution {

        /**
         * Regex matching any Unicode space separator (\p{Zs}).
         * Notification-formatted amounts must NEVER contain these because
         * Android's RemoteViews treats them all as line-break opportunities.
         */
        private val unicodeSpaceRegex = Regex("[\\p{Zs}]")

        @Test
        fun `replaces NBSP with braille blank in suffix-currency locale`() {
            every { localeProvider.getCurrentLocale() } returns Locale.forLanguageTag("es-ES")
            val data = mapOf("amountCents" to "10050", "currencyCode" to "EUR")
            val result = formatNotificationAmount(data, localeProvider)
            // Spanish locale: "100,50 €" — NBSP replaced with \u2800
            assertFalse(
                unicodeSpaceRegex.containsMatchIn(result),
                "Unicode space separator found in \"$result\" — should use \\u2800 instead"
            )
            assertEquals("100,50\u2800€", result)
        }

        @Test
        fun `prefix-currency locale has no space to replace`() {
            every { localeProvider.getCurrentLocale() } returns Locale.US
            val data = mapOf("amountCents" to "10050", "currencyCode" to "USD")
            val result = formatNotificationAmount(data, localeProvider)
            // US locale places symbol before number with no space: "$100.50"
            assertEquals("$100.50", result)
        }

        @Test
        fun `output never contains any Unicode space separator`() {
            val locales = listOf(Locale.US, Locale.forLanguageTag("es-ES"), Locale.FRANCE, Locale.JAPAN)
            val currencies = listOf("EUR", "USD", "GBP", "JPY", "THB", "MXN")

            for (locale in locales) {
                every { localeProvider.getCurrentLocale() } returns locale
                for (currency in currencies) {
                    val data = mapOf("amountCents" to "50000", "currencyCode" to currency)
                    val result = formatNotificationAmount(data, localeProvider)
                    assertFalse(
                        unicodeSpaceRegex.containsMatchIn(result),
                        "Unicode space found in \"$result\" for $currency / $locale"
                    )
                }
            }
        }
    }

    // ---------- Missing / invalid data ----------

    @Nested
    @DisplayName("Missing data fallbacks")
    inner class MissingData {

        @Test
        fun `returns empty string when amountCents is missing`() {
            val data = mapOf("currencyCode" to "EUR")
            assertEquals("", formatNotificationAmount(data, localeProvider))
        }

        @Test
        fun `returns empty string when currencyCode is missing`() {
            val data = mapOf("amountCents" to "1000")
            assertEquals("", formatNotificationAmount(data, localeProvider))
        }

        @Test
        fun `returns empty string when amountCents is not a number`() {
            val data = mapOf("amountCents" to "abc", "currencyCode" to "EUR")
            assertEquals("", formatNotificationAmount(data, localeProvider))
        }
    }
}
