package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleaner
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UnregisterDeviceTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SignOutUseCaseTest {

    private lateinit var unregisterDeviceTokenUseCase: UnregisterDeviceTokenUseCase
    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var localDatabaseCleaner: LocalDatabaseCleaner
    private lateinit var authenticationService: AuthenticationService
    private lateinit var useCase: SignOutUseCase

    @BeforeEach
    fun setUp() {
        unregisterDeviceTokenUseCase = mockk()
        preferenceRepository = mockk()
        localDatabaseCleaner = mockk()
        authenticationService = mockk()
        useCase = SignOutUseCase(
            unregisterDeviceTokenUseCase = unregisterDeviceTokenUseCase,
            preferenceRepository = preferenceRepository,
            localDatabaseCleaner = localDatabaseCleaner,
            authenticationService = authenticationService
        )
    }

    @Nested
    inner class SuccessPath {

        @Test
        fun `returns success when all cleanup steps succeed`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then
            assertTrue(result.isSuccess)
        }

        @Test
        fun `executes cleanup steps in correct order`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            useCase()

            // Then — auth sign-out before Room clear to terminate snapshot listeners
            coVerifyOrder {
                unregisterDeviceTokenUseCase()
                preferenceRepository.clearAll()
                authenticationService.signOut()
                localDatabaseCleaner.clearAll()
            }
        }

        @Test
        fun `succeeds even when device token unregistration fails`() = runTest {
            // Given - device token unregistration fails (best-effort)
            coEvery { unregisterDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then - sign-out should still succeed
            assertTrue(result.isSuccess)
        }

        @Test
        fun `all steps execute even when device token unregistration fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            useCase()

            // Then
            coVerify(exactly = 1) { preferenceRepository.clearAll() }
            coVerify(exactly = 1) { authenticationService.signOut() }
            coVerify(exactly = 1) { localDatabaseCleaner.clearAll() }
        }
    }

    @Nested
    inner class ResiliencePaths {

        @Test
        fun `always attempts auth sign-out even when clearing preferences throws`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } throws RuntimeException("DataStore error")
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then — auth sign-out and Room clear still executed
            coVerify(exactly = 1) { authenticationService.signOut() }
            coVerify(exactly = 1) { localDatabaseCleaner.clearAll() }
            // Cleanup failure is surfaced since sign-out succeeded
            assertTrue(result.isFailure)
        }

        @Test
        fun `returns cleanup failure when sign-out succeeds but cleanup fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } throws RuntimeException("DataStore error")
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then — cleanup failure is surfaced
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message == "DataStore error")
        }

        @Test
        fun `returns db clear failure when preferences clear succeeds but db clear fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } throws RuntimeException("DB error")

            // When
            val result = useCase()

            // Then — db clear failure is surfaced
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message == "DB error")
        }
    }

    @Nested
    inner class FailurePaths {

        @Test
        fun `returns sign-out failure when auth sign-out fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.failure(RuntimeException("Auth error"))
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
        }

        @Test
        fun `prioritizes sign-out failure over cleanup failure`() = runTest {
            // Given — both cleanup and sign-out fail
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } throws RuntimeException("DataStore error")
            coEvery { authenticationService.signOut() } returns Result.failure(RuntimeException("Auth error"))
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then — sign-out failure takes priority
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message == "Auth error")
        }
    }
}

