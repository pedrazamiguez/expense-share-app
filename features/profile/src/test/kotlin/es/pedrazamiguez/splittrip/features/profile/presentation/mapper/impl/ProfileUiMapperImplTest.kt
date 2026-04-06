package es.pedrazamiguez.splittrip.features.profile.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.domain.model.User
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileUiMapperImplTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var mapper: ProfileUiMapperImpl

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
        every { localeProvider.getCurrentLocale() } returns Locale.US
        mapper = ProfileUiMapperImpl(localeProvider)
    }

    @Nested
    inner class ToProfileUiModel {

        @Test
        fun `maps full user with all fields`() {
            val user = User(
                userId = "user-1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImagePath = "https://example.com/photo.jpg",
                createdAt = LocalDateTime.of(2024, 6, 15, 10, 30)
            )

            val result = mapper.toProfileUiModel(user)

            assertEquals("John Doe", result.displayName)
            assertEquals("john@example.com", result.email)
            assertEquals("https://example.com/photo.jpg", result.profileImageUrl)
            // memberSinceText should be non-empty (formatted date)
            assertEquals(true, result.memberSinceText.isNotEmpty())
        }

        @Test
        fun `falls back to email when displayName is null`() {
            val user = User(
                userId = "user-2",
                email = "jane@example.com",
                displayName = null
            )

            val result = mapper.toProfileUiModel(user)

            assertEquals("jane@example.com", result.displayName)
        }

        @Test
        fun `sets profileImageUrl to null when profileImagePath is null`() {
            val user = User(
                userId = "user-3",
                email = "test@example.com",
                profileImagePath = null
            )

            val result = mapper.toProfileUiModel(user)

            assertNull(result.profileImageUrl)
        }

        @Test
        fun `sets memberSinceText to empty string when createdAt is null`() {
            val user = User(
                userId = "user-4",
                email = "test@example.com",
                createdAt = null
            )

            val result = mapper.toProfileUiModel(user)

            assertEquals("", result.memberSinceText)
        }
    }
}
