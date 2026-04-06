package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.NotificationUserPreferences
import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NotificationPreferencesRepositoryImpl")
class NotificationPreferencesRepositoryImplTest {

    private lateinit var notificationUserPreferences: NotificationUserPreferences
    private lateinit var repository: NotificationPreferencesRepositoryImpl

    @BeforeEach
    fun setUp() {
        notificationUserPreferences = mockk(relaxed = true)
        repository = NotificationPreferencesRepositoryImpl(notificationUserPreferences)
    }

    @Nested
    @DisplayName("getPreferencesFlow")
    inner class GetPreferencesFlow {

        @Test
        fun `combines all three preference flows`() = runTest {
            every { notificationUserPreferences.notificationMembershipEnabled } returns flowOf(true)
            every { notificationUserPreferences.notificationExpensesEnabled } returns flowOf(false)
            every { notificationUserPreferences.notificationFinancialEnabled } returns flowOf(true)

            val result = repository.getPreferencesFlow().first()

            assertTrue(result.membershipEnabled)
            assertFalse(result.expensesEnabled)
            assertTrue(result.financialEnabled)
        }

        @Test
        fun `all disabled when all flows emit false`() = runTest {
            every { notificationUserPreferences.notificationMembershipEnabled } returns flowOf(false)
            every { notificationUserPreferences.notificationExpensesEnabled } returns flowOf(false)
            every { notificationUserPreferences.notificationFinancialEnabled } returns flowOf(false)

            val result = repository.getPreferencesFlow().first()

            assertFalse(result.membershipEnabled)
            assertFalse(result.expensesEnabled)
            assertFalse(result.financialEnabled)
        }
    }

    @Nested
    @DisplayName("updatePreference")
    inner class UpdatePreference {

        @Test
        fun `MEMBERSHIP category calls setNotificationMembershipEnabled`() = runTest {
            repository.updatePreference(NotificationCategory.MEMBERSHIP, false)
            coVerify { notificationUserPreferences.setNotificationMembershipEnabled(false) }
        }

        @Test
        fun `EXPENSES category calls setNotificationExpensesEnabled`() = runTest {
            repository.updatePreference(NotificationCategory.EXPENSES, true)
            coVerify { notificationUserPreferences.setNotificationExpensesEnabled(true) }
        }

        @Test
        fun `FINANCIAL category calls setNotificationFinancialEnabled`() = runTest {
            repository.updatePreference(NotificationCategory.FINANCIAL, false)
            coVerify { notificationUserPreferences.setNotificationFinancialEnabled(false) }
        }
    }

    @Nested
    @DisplayName("defaults")
    inner class Defaults {

        @Test
        fun `all preferences default to true when DataStore has no values`() = runTest {
            every { notificationUserPreferences.notificationMembershipEnabled } returns flowOf(true)
            every { notificationUserPreferences.notificationExpensesEnabled } returns flowOf(true)
            every { notificationUserPreferences.notificationFinancialEnabled } returns flowOf(true)

            val result = repository.getPreferencesFlow().first()

            assertEquals(true, result.membershipEnabled)
            assertEquals(true, result.expensesEnabled)
            assertEquals(true, result.financialEnabled)
        }
    }
}
