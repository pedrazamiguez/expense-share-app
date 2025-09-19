package es.pedrazamiguez.expenseshareapp.domain.usecase

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetUserBalanceUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val getUserBalanceUseCase = GetUserBalanceUseCase(userRepository)

    @Test
    fun `returns balance successfully`() = runTest {
        val userId = "user-123"
        val balance = 42.0

        coEvery { userRepository.getUserBalance(userId) } returns balance

        val result = getUserBalanceUseCase(userId)

        assertTrue(result.isSuccess)
        assertEquals(balance, result.getOrThrow())
    }

    @Test
    fun `returns failure on exception`() = runTest {
        val userId = "user-123"
        val exception = Exception("Something went wrong")

        coEvery { userRepository.getUserBalance(userId) } throws exception

        val result = getUserBalanceUseCase(userId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

}
