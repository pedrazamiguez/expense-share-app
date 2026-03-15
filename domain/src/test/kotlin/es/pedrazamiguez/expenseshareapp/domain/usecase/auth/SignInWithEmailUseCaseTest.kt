package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SignInWithEmailUseCaseTest {

    private lateinit var authenticationService: AuthenticationService
    private lateinit var registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
    private lateinit var useCase: SignInWithEmailUseCase

    private val email = "user@example.com"
    private val password = "password123"
    private val userId = "firebase-uid-123"

    @BeforeEach
    fun setUp() {
        authenticationService = mockk()
        registerDeviceTokenUseCase = mockk()
        useCase = SignInWithEmailUseCase(
            authenticationService = authenticationService,
            registerDeviceTokenUseCase = registerDeviceTokenUseCase
        )
    }

    @Nested
    inner class SuccessPath {

        @Test
        fun `returns userId on successful sign-in`() = runTest {
            // Given
            coEvery { authenticationService.signIn(email, password) } returns Result.success(userId)
            coEvery { registerDeviceTokenUseCase() } returns Result.success(Unit)

            // When
            val result = useCase(email, password)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(userId, result.getOrNull())
        }

        @Test
        fun `calls signIn with correct email and password`() = runTest {
            // Given
            coEvery { authenticationService.signIn(any(), any()) } returns Result.success(userId)
            coEvery { registerDeviceTokenUseCase() } returns Result.success(Unit)

            // When
            useCase(email, password)

            // Then
            coVerify(exactly = 1) { authenticationService.signIn(email, password) }
        }

        @Test
        fun `registers device token after successful sign-in`() = runTest {
            // Given
            coEvery { authenticationService.signIn(email, password) } returns Result.success(userId)
            coEvery { registerDeviceTokenUseCase() } returns Result.success(Unit)

            // When
            useCase(email, password)

            // Then
            coVerify(exactly = 1) { registerDeviceTokenUseCase() }
        }

        @Test
        fun `succeeds even when device token registration fails`() = runTest {
            // Given
            coEvery { authenticationService.signIn(email, password) } returns Result.success(userId)
            coEvery { registerDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))

            // When
            val result = useCase(email, password)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(userId, result.getOrNull())
        }
    }

    @Nested
    inner class FailurePath {

        @Test
        fun `fails when authentication fails`() = runTest {
            // Given
            val exception = RuntimeException("Auth failed")
            coEvery { authenticationService.signIn(email, password) } returns Result.failure(exception)

            // When
            val result = useCase(email, password)

            // Then
            assertTrue(result.isFailure)
            assertEquals("Auth failed", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { registerDeviceTokenUseCase() }
        }
    }
}
