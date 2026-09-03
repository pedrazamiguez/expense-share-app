package es.pedrazamiguez.splittrip.domain.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SubscriptionTierTest {

    @Nested
    inner class FromString {

        @ParameterizedTest
        @CsvSource(
            "FREE, FREE",
            "PRO, PRO",
            "free, FREE",
            "pro, PRO",
            "Free, FREE",
            "Pro, PRO"
        )
        fun `parses known tiers case-insensitively`(input: String, expected: SubscriptionTier) {
            assertEquals(expected, SubscriptionTier.fromString(input))
        }

        @Test
        fun `throws IllegalArgumentException for unknown tier string`() {
            val ex = assertThrows(IllegalArgumentException::class.java) {
                SubscriptionTier.fromString("ENTERPRISE")
            }
            assertEquals("Unknown subscription tier: ENTERPRISE", ex.message)
        }
    }

    @Nested
    inner class FromStringOrDefault {

        @ParameterizedTest
        @CsvSource(
            "FREE, FREE",
            "PRO, PRO",
            "free, FREE",
            "pro, PRO"
        )
        fun `returns parsed tier for known strings`(input: String, expected: SubscriptionTier) {
            assertEquals(expected, SubscriptionTier.fromStringOrDefault(input))
        }

        @Test
        fun `returns FREE for null input`() {
            assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromStringOrDefault(null))
        }

        @Test
        fun `returns FREE for invalid input`() {
            assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromStringOrDefault("UNKNOWN"))
        }
    }
}
