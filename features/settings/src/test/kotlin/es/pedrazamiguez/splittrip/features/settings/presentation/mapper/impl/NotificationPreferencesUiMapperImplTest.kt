package es.pedrazamiguez.splittrip.features.settings.presentation.mapper.impl

import es.pedrazamiguez.splittrip.domain.model.NotificationPreferences
import es.pedrazamiguez.splittrip.domain.model.User
import java.time.ZoneId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotificationPreferencesUiMapperImplTest {

    private lateinit var mapper: NotificationPreferencesUiMapperImpl

    @BeforeEach
    fun setUp() {
        mapper = NotificationPreferencesUiMapperImpl()
    }

    @Test
    fun `toUiState maps preferences and user correctly`() {
        // Given
        val prefs = NotificationPreferences(
            membershipEnabled = true,
            expensesEnabled = false,
            financialEnabled = true
        )
        val user = User(
            userId = "test-user-id",
            email = "user@test.com",
            displayName = "Test User",
            timezone = "Europe/Madrid",
            preferredReminderTime = "12:30"
        )

        // When
        val state = mapper.toUiState(prefs, user)

        // Then
        assertEquals(true, state.membershipEnabled)
        assertEquals(false, state.expensesEnabled)
        assertEquals(true, state.financialEnabled)
        assertEquals("Europe/Madrid", state.timezone)
        assertEquals("12:30", state.preferredReminderTime)
        assertFalse(state.isLoading)
    }

    @Test
    fun `toUiState fallback to system default timezone and null preferredReminderTime when user is null`() {
        // Given
        val prefs = NotificationPreferences(
            membershipEnabled = false,
            expensesEnabled = true,
            financialEnabled = false
        )

        // When
        val state = mapper.toUiState(prefs, null)

        // Then
        assertEquals(false, state.membershipEnabled)
        assertEquals(true, state.expensesEnabled)
        assertEquals(false, state.financialEnabled)
        assertEquals(ZoneId.systemDefault().id, state.timezone)
        assertNull(state.preferredReminderTime)
        assertFalse(state.isLoading)
    }

    @Test
    fun `toUiState fallback to system default timezone when user has null timezone`() {
        // Given
        val prefs = NotificationPreferences()
        val user = User(
            userId = "test-user-id",
            email = "user@test.com",
            displayName = "Test User",
            timezone = null,
            preferredReminderTime = "15:45"
        )

        // When
        val state = mapper.toUiState(prefs, user)

        // Then
        assertEquals(ZoneId.systemDefault().id, state.timezone)
        assertEquals("15:45", state.preferredReminderTime)
    }

    @Test
    fun `formatTime formats hours and minutes with leading zeros`() {
        assertEquals("00:00", mapper.formatTime(0, 0))
        assertEquals("09:05", mapper.formatTime(9, 5))
        assertEquals("12:34", mapper.formatTime(12, 34))
        assertEquals("23:59", mapper.formatTime(23, 59))
    }
}
