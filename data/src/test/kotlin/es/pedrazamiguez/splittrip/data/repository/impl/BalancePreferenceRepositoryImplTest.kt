package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("BalancePreferenceRepositoryImpl")
class BalancePreferenceRepositoryImplTest {

    private lateinit var userPreferences: UserPreferences
    private lateinit var repository: BalancePreferenceRepositoryImpl

    @BeforeEach
    fun setUp() {
        userPreferences = mockk(relaxed = true)
        repository = BalancePreferenceRepositoryImpl(userPreferences)
    }

    @Nested
    @DisplayName("getLastSeenBalance")
    inner class GetLastSeenBalance {

        @Test
        fun `returns stored balance from user preferences`() = runTest {
            val groupId = "group-123"
            val expectedBalance = "€42.50"
            every { userPreferences.getLastSeenBalance(groupId) } returns flowOf(expectedBalance)

            val result = repository.getLastSeenBalance(groupId).first()

            assertEquals(expectedBalance, result)
        }

        @Test
        fun `returns null when no balance stored`() = runTest {
            val groupId = "group-456"
            every { userPreferences.getLastSeenBalance(groupId) } returns flowOf(null)

            val result = repository.getLastSeenBalance(groupId).first()

            assertNull(result)
        }
    }

    @Nested
    @DisplayName("setLastSeenBalance")
    inner class SetLastSeenBalance {

        @Test
        fun `delegates to user preferences`() = runTest {
            val groupId = "group-123"
            val formattedBalance = "€100.00"

            repository.setLastSeenBalance(groupId, formattedBalance)

            coVerify { userPreferences.setLastSeenBalance(groupId, formattedBalance) }
        }
    }
}
