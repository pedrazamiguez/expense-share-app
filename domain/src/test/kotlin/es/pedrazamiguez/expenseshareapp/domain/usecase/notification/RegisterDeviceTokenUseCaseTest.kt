package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.repository.DeviceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("RegisterDeviceTokenUseCase")
class RegisterDeviceTokenUseCaseTest {

    private lateinit var deviceRepository: DeviceRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var useCase: RegisterDeviceTokenUseCase

    private val deviceToken = "device-token-abc"

    @BeforeEach
    fun setUp() {
        deviceRepository = mockk()
        notificationRepository = mockk()
        useCase = RegisterDeviceTokenUseCase(
            deviceRepository = deviceRepository,
            notificationRepository = notificationRepository
        )
    }

    @Nested
    @DisplayName("success path")
    inner class SuccessPath {

        @Test
        fun `returns success when token is registered`() = runTest {
            coEvery { deviceRepository.getDeviceToken() } returns Result.success(deviceToken)
            coEvery { notificationRepository.registerDeviceTokenWithRetry(deviceToken) } just Runs

            val result = useCase()

            assertTrue(result.isSuccess)
        }

        @Test
        fun `calls registerDeviceTokenWithRetry with correct token`() = runTest {
            coEvery { deviceRepository.getDeviceToken() } returns Result.success(deviceToken)
            coEvery { notificationRepository.registerDeviceTokenWithRetry(deviceToken) } just Runs

            useCase()

            coVerify(exactly = 1) { notificationRepository.registerDeviceTokenWithRetry(deviceToken) }
        }
    }

    @Nested
    @DisplayName("failure path")
    inner class FailurePath {

        @Test
        fun `returns failure when getDeviceToken fails`() = runTest {
            coEvery { deviceRepository.getDeviceToken() } returns Result.failure(
                RuntimeException("FCM unavailable")
            )

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals("FCM unavailable", result.exceptionOrNull()?.message)
        }

        @Test
        fun `returns failure when registerDeviceTokenWithRetry throws`() = runTest {
            coEvery { deviceRepository.getDeviceToken() } returns Result.success(deviceToken)
            coEvery {
                notificationRepository.registerDeviceTokenWithRetry(deviceToken)
            } throws RuntimeException("Network error")

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }

        @Test
        fun `does not call registerDeviceTokenWithRetry when getDeviceToken fails`() = runTest {
            coEvery { deviceRepository.getDeviceToken() } returns Result.failure(
                RuntimeException("FCM unavailable")
            )

            useCase()

            coVerify(exactly = 0) { notificationRepository.registerDeviceTokenWithRetry(any()) }
        }
    }
}

