package es.pedrazamiguez.expenseshareapp.features.main.navigation

import android.net.Uri
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DeepLinkHolder")
class DeepLinkHolderTest {

    private lateinit var holder: DeepLinkHolder

    @BeforeEach
    fun setUp() {
        holder = DeepLinkHolder()
    }

    @Nested
    @DisplayName("pendingDeepLink")
    inner class PendingDeepLink {

        @Test
        fun `initial value is null`() {
            assertNull(holder.pendingDeepLink)
        }

        @Test
        fun `stores a deep link URI`() {
            // Given
            val uri = mockk<Uri>()

            // When
            holder.pendingDeepLink = uri

            // Then
            assertEquals(uri, holder.pendingDeepLink)
        }

        @Test
        fun `overwrites previous deep link`() {
            // Given
            val uri1 = mockk<Uri>()
            val uri2 = mockk<Uri>()
            holder.pendingDeepLink = uri1

            // When
            holder.pendingDeepLink = uri2

            // Then
            assertEquals(uri2, holder.pendingDeepLink)
        }

        @Test
        fun `can be cleared by setting null`() {
            // Given
            holder.pendingDeepLink = mockk<Uri>()

            // When
            holder.pendingDeepLink = null

            // Then
            assertNull(holder.pendingDeepLink)
        }
    }

    @Nested
    @DisplayName("consumePendingDeepLink")
    inner class ConsumePendingDeepLink {

        @Test
        fun `returns null when no deep link is pending`() {
            assertNull(holder.consumePendingDeepLink())
        }

        @Test
        fun `returns and clears the pending deep link`() {
            // Given
            val uri = mockk<Uri>()
            holder.pendingDeepLink = uri

            // When
            val consumed = holder.consumePendingDeepLink()

            // Then
            assertNotNull(consumed)
            assertEquals(uri, consumed)
            assertNull(holder.pendingDeepLink)
        }

        @Test
        fun `returns null on second call after consumption`() {
            // Given
            holder.pendingDeepLink = mockk<Uri>()
            holder.consumePendingDeepLink() // First consumption

            // When
            val secondCall = holder.consumePendingDeepLink()

            // Then
            assertNull(secondCall)
        }
    }
}
