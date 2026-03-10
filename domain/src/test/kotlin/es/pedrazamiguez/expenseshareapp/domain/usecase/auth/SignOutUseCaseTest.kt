package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

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
    private lateinit var localDatabaseCleaner: LocalDatabaseCleaner
    private lateinit var authenticationService: AuthenticationService
    private lateinit var useCase: SignOutUseCase

    @BeforeEach
    fun setUp() {
        unregisterDeviceTokenUseCase = mockk()
        localDatabaseCleaner = mockk()
        authenticationService = mockk()
        useCase = SignOutUseCase(
            unregisterDeviceTokenUseCase = unregisterDeviceTokenUseCase,
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
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            useCase()

            // Then — auth sign-out before Room clear to terminate snapshot listeners
            coVerifyOrder {
                unregisterDeviceTokenUseCase()
                authenticationService.signOut()
                localDatabaseCleaner.clearAll()
            }
        }

        @Test
        fun `does not clear DataStore preferences (keys are user-scoped)`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            useCase()

            // Then — only Room is cleared, DataStore is preserved
            coVerify(exactly = 1) { localDatabaseCleaner.clearAll() }
            coVerify(exactly = 1) { authenticationService.signOut() }
        }

        @Test
        fun `succeeds even when device token unregistration fails`() = runTest {
            // Given - device token unregistration fails (best-effort)
            coEvery { unregisterDeviceTokenUseCase() } returns Result.failure(RuntimeException("Token failed"))
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
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            useCase()

            // Then
            coVerify(exactly = 1) { authenticationService.signOut() }
            coVerify(exactly = 1) { localDatabaseCleaner.clearAll() }
        }
    }

    @Nested
    inner class ResiliencePaths {

        @Test
        fun `returns db clear failure when sign-out succeeds but db clear fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { authenticationService.signOut() } returns Result.success(Unit)
            coEvery { localDatabaseCleaner.clearAll() } throws RuntimeException("DB error")

            // When
            val result = useCase()

            // Then — db clear failure is surfaced
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message == "DB error")
        }

        @Test
        fun `always attempts Room clear even when auth sign-out fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { authenticationService.signOut() } returns Result.failure(RuntimeException("Auth error"))
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            useCase()

            // Then — Room clear still executed
            coVerify(exactly = 1) { localDatabaseCleaner.clearAll() }
        }
    }

    @Nested
    inner class FailurePaths {

        @Test
        fun `returns sign-out failure when auth sign-out fails`() = runTest {
            // Given
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { authenticationService.signOut() } returns Result.failure(RuntimeException("Auth error"))
            coEvery { localDatabaseCleaner.clearAll() } returns Unit

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
        }

        @Test
        fun `prioritizes sign-out failure over db cleanup failure`() = runTest {
            // Given — both db clear and sign-out fail
            coEvery { unregisterDeviceTokenUseCase() } returns Result.success(Unit)
            coEvery { authenticationService.signOut() } returns Result.failure(RuntimeException("Auth error"))
            coEvery { localDatabaseCleaner.clearAll() } throws RuntimeException("DB error")

            // When
            val result = useCase()

            // Then — sign-out failure takes priority
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message == "Auth error")
        }
    }
}
