package es.pedrazamiguez.splittrip.domain.usecase.user.impl

import es.pedrazamiguez.splittrip.domain.repository.UserRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UpdateUserReminderPreferencesUseCaseImpl")
class UpdateUserReminderPreferencesUseCaseImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: UpdateUserReminderPreferencesUseCaseImpl

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        useCase = UpdateUserReminderPreferencesUseCaseImpl(userRepository)
    }

    @Test
    @DisplayName("returns failure when repository fails")
    fun returnsFailure() = runTest {
        val error = RuntimeException("Error")
        io.mockk.coEvery {
            userRepository.updateUserReminderPreferences(any(), any(), any())
        } returns Result.failure(error)

        val result = useCase("testUser", "Europe/London", "10:00")

        org.junit.jupiter.api.Assertions.assertEquals(Result.failure<Unit>(error), result)
    }

    @Test
    @DisplayName("returns success when repository succeeds")
    fun returnsSuccess() = runTest {
        io.mockk.coEvery {
            userRepository.updateUserReminderPreferences(any(), any(), any())
        } returns Result.success(Unit)

        val result = useCase("testUser", "Europe/London", "10:00")

        org.junit.jupiter.api.Assertions.assertEquals(Result.success(Unit), result)
    }

    @Test
    @DisplayName("returns success when repository succeeds with nulls")
    fun returnsSuccessWithNulls() = runTest {
        io.mockk.coEvery {
            userRepository.updateUserReminderPreferences(any(), any(), any())
        } returns Result.success(Unit)

        val result = useCase("testUser", null, null)

        org.junit.jupiter.api.Assertions.assertEquals(Result.success(Unit), result)
    }
}
