package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationRepositoryImplTest {

    private val authenticationService = mockk<AuthenticationService>()
    private val repository = AuthenticationRepositoryImpl(authenticationService)

    @Test
    fun `signIn returns user uid on success`() = runTest {
        val uid = "test-uid"

        coEvery {
            authenticationService.signIn(
                "test@example.com",
                "password"
            )
        } returns Result.success(uid)

        val result = repository.signIn(
            "test@example.com",
            "password"
        )

        assertEquals(
            uid,
            result.getOrThrow()
        )
    }

    @Test
    fun `signIn returns failure on exception`() = runTest {
        val exception = Exception("SignIn failed")

        coEvery {
            authenticationService.signIn(
                "test@example.com",
                "password"
            )
        } returns Result.failure(exception)

        val result = repository.signIn(
            "test@example.com",
            "password"
        )

        assertTrue(result.isFailure)
        assertEquals(
            exception,
            result.exceptionOrNull()
        )
    }

    @Test
    fun `signUp returns user uid on success`() = runTest {
        val uid = "new-user-uid"

        coEvery {
            authenticationService.signUp(
                "new@example.com",
                "password"
            )
        } returns Result.success(uid)

        val result = repository.signUp(
            "new@example.com",
            "password"
        )

        assertEquals(
            uid,
            result.getOrThrow()
        )
    }

    @Test
    fun `signUp returns failure on exception`() = runTest {
        val exception = Exception("SignUp failed")

        coEvery {
            authenticationService.signUp(
                "new@example.com",
                "password"
            )
        } returns Result.failure(exception)

        val result = repository.signUp(
            "new@example.com",
            "password"
        )

        assertTrue(result.isFailure)
        assertEquals(
            exception,
            result.exceptionOrNull()
        )
    }

}
