package es.pedrazamiguez.splittrip.domain.usecase.user.impl

import es.pedrazamiguez.splittrip.domain.enums.SubscriptionTier
import es.pedrazamiguez.splittrip.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UpdateUserTierUseCaseImpl")
class UpdateUserTierUseCaseImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: UpdateUserTierUseCaseImpl

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        useCase = UpdateUserTierUseCaseImpl(userRepository)
    }

    @Test
    @DisplayName("returns failure when repository fails")
    fun returnsFailure() = runTest {
        val error = RuntimeException("Database error")
        coEvery {
            userRepository.updateUserTier(any(), any())
        } returns Result.failure(error)

        val result = useCase("testUser", SubscriptionTier.PRO)

        assertEquals(Result.failure<Unit>(error), result)
        coVerify(exactly = 1) { userRepository.updateUserTier("testUser", SubscriptionTier.PRO) }
    }

    @Test
    @DisplayName("returns success when repository succeeds")
    fun returnsSuccess() = runTest {
        coEvery {
            userRepository.updateUserTier(any(), any())
        } returns Result.success(Unit)

        val result = useCase("testUser", SubscriptionTier.PRO)

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { userRepository.updateUserTier("testUser", SubscriptionTier.PRO) }
    }
}
