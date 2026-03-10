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
            coEvery { localDatabaseCleaner.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)

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
            coEvery { localDatabaseCleaner.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)

            // When
            useCase()

            // Then
            coVerifyOrder {
                unregisterDeviceTokenUseCase()
                preferenceRepository.clearAll()
                localDatabaseCleaner.clearAll()
                authenticationService.signOut()
            }
        }

        @Test
        fun `succeeds even when device token unregistration fails`() = runTest {
            // Given - device token unregistration fails (best-effort)
            coEvery { unregisterDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { localDatabaseCleaner.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)

            // When
            val result = useCase()

            // Then - sign-out should still succeed
            assertTrue(result.isSuccess)
        }

        @Test
        fun `clears preferences even when device token unregistration fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { localDatabaseCleaner.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.success(Unit)

            // When
            useCase()

            // Then
            coVerify(exactly = 1) { preferenceRepository.clearAll() }
            coVerify(exactly = 1) { localDatabaseCleaner.clearAll() }
            coVerify(exactly = 1) { authenticationService.signOut() }
        }
    }

    @Nested
    inner class FailurePaths {

        @Test
        fun `fails when clearing preferences throws`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } throws RuntimeException("DataStore error")

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
            coVerify(exactly = 0) { localDatabaseCleaner.clearAll() }
            coVerify(exactly = 0) { authenticationService.signOut() }
        }

        @Test
        fun `fails when clearing database throws`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { localDatabaseCleaner.clearAll() } throws RuntimeException("DB error")

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
            coVerify(exactly = 0) { authenticationService.signOut() }
        }

        @Test
        fun `fails when authentication sign-out fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { preferenceRepository.clearAll() } returns Unit
            coEvery { localDatabaseCleaner.clearAll() } returns Unit
            coEvery { authenticationService.signOut() } returns Result.failure(RuntimeException("Auth error"))

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
        }
    }
}

