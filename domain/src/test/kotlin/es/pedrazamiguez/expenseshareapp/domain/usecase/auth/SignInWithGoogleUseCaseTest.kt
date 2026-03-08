package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
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

class SignInWithGoogleUseCaseTest {

    private lateinit var authenticationService: AuthenticationService
    private lateinit var userRepository: UserRepository
    private lateinit var registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
    private lateinit var useCase: SignInWithGoogleUseCase

    private val idToken = "google-id-token"
    private val firebaseUser = User(
        userId = "firebase-uid-123",
        email = "user@example.com",
        displayName = "Test User",
        profileImagePath = "https://example.com/photo.jpg"
    )

    @BeforeEach
    fun setUp() {
        authenticationService = mockk()
        userRepository = mockk()
        registerDeviceTokenUseCase = mockk()
        useCase = SignInWithGoogleUseCase(
            authenticationService = authenticationService,
            userRepository = userRepository,
            registerDeviceTokenUseCase = registerDeviceTokenUseCase
        )
    }

    @Nested
    inner class SuccessPath {

        @Test
        fun `returns userId on successful sign-in`() = runTest {
            // Given
            coEvery { authenticationService.signInWithGoogle(idToken) } returns Result.success(firebaseUser)
            coEvery { userRepository.saveGoogleUser(any()) } returns Result.success(Unit)
            coEvery { registerDeviceTokenUseCase() } returns Result.success(Unit)

            // When
            val result = useCase(idToken)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(firebaseUser.userId, result.getOrNull())
        }

        @Test
        fun `saves user returned by authentication service`() = runTest {
            // Given
            coEvery { authenticationService.signInWithGoogle(idToken) } returns Result.success(firebaseUser)
            coEvery { userRepository.saveGoogleUser(any()) } returns Result.success(Unit)
            coEvery { registerDeviceTokenUseCase() } returns Result.success(Unit)

            // When
            useCase(idToken)

            // Then
            coVerify(exactly = 1) { userRepository.saveGoogleUser(firebaseUser) }
        }

        @Test
        fun `registers device token after saving user`() = runTest {
            // Given
            coEvery { authenticationService.signInWithGoogle(idToken) } returns Result.success(firebaseUser)
            coEvery { userRepository.saveGoogleUser(any()) } returns Result.success(Unit)
            coEvery { registerDeviceTokenUseCase() } returns Result.success(Unit)

            // When
            useCase(idToken)

            // Then
            coVerify(exactly = 1) { registerDeviceTokenUseCase() }
        }

        @Test
        fun `succeeds even when device token registration fails`() = runTest {
            // Given
            coEvery { authenticationService.signInWithGoogle(idToken) } returns Result.success(firebaseUser)
            coEvery { userRepository.saveGoogleUser(any()) } returns Result.success(Unit)
            coEvery { registerDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))

            // When
            val result = useCase(idToken)

            // Then - sign-in should still succeed (device token is best-effort)
            assertTrue(result.isSuccess)
            assertEquals(firebaseUser.userId, result.getOrNull())
        }
    }

    @Nested
    inner class FailurePaths {

        @Test
        fun `fails when authentication fails`() = runTest {
            // Given
            val exception = RuntimeException("Auth failed")
            coEvery { authenticationService.signInWithGoogle(idToken) } returns Result.failure(exception)

            // When
            val result = useCase(idToken)

            // Then
            assertTrue(result.isFailure)
            assertEquals("Auth failed", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { userRepository.saveGoogleUser(any()) }
            coVerify(exactly = 0) { registerDeviceTokenUseCase() }
        }

        @Test
        fun `fails when saving user fails`() = runTest {
            // Given
            val exception = RuntimeException("Save user failed")
            coEvery { authenticationService.signInWithGoogle(idToken) } returns Result.success(firebaseUser)
            coEvery { userRepository.saveGoogleUser(any()) } returns Result.failure(exception)

            // When
            val result = useCase(idToken)

            // Then
            assertTrue(result.isFailure)
            assertEquals("Save user failed", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { registerDeviceTokenUseCase() }
        }
    }
}


