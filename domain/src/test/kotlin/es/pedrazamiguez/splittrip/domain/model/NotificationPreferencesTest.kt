package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NotificationPreferences")
class NotificationPreferencesTest {

    @Nested
    @DisplayName("isCategoryEnabled")
    inner class IsCategoryEnabled {

        @Test
        fun `returns true when membership is enabled`() {
            val prefs = NotificationPreferences(membershipEnabled = true)
            assertTrue(prefs.isCategoryEnabled(NotificationCategory.MEMBERSHIP))
        }

        @Test
        fun `returns false when membership is disabled`() {
            val prefs = NotificationPreferences(membershipEnabled = false)
            assertFalse(prefs.isCategoryEnabled(NotificationCategory.MEMBERSHIP))
        }

        @Test
        fun `returns true when expenses is enabled`() {
            val prefs = NotificationPreferences(expensesEnabled = true)
            assertTrue(prefs.isCategoryEnabled(NotificationCategory.EXPENSES))
        }

        @Test
        fun `returns false when expenses is disabled`() {
            val prefs = NotificationPreferences(expensesEnabled = false)
            assertFalse(prefs.isCategoryEnabled(NotificationCategory.EXPENSES))
        }

        @Test
        fun `returns true when financial is enabled`() {
            val prefs = NotificationPreferences(financialEnabled = true)
            assertTrue(prefs.isCategoryEnabled(NotificationCategory.FINANCIAL))
        }

        @Test
        fun `returns false when financial is disabled`() {
            val prefs = NotificationPreferences(financialEnabled = false)
            assertFalse(prefs.isCategoryEnabled(NotificationCategory.FINANCIAL))
        }

        @Test
        fun `all defaults are enabled`() {
            val prefs = NotificationPreferences()
            assertTrue(prefs.isCategoryEnabled(NotificationCategory.MEMBERSHIP))
            assertTrue(prefs.isCategoryEnabled(NotificationCategory.EXPENSES))
            assertTrue(prefs.isCategoryEnabled(NotificationCategory.FINANCIAL))
        }
    }
}
