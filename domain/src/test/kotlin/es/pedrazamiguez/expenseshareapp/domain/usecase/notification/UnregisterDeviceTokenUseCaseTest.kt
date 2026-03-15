package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.repository.DeviceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnregisterDeviceTokenUseCaseTest {

    private lateinit var deviceRepository: DeviceRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var useCase: UnregisterDeviceTokenUseCase

    private val deviceToken = "device-token-abc"

    @BeforeEach
    fun setUp() {
        deviceRepository = mockk()
        notificationRepository = mockk()
        useCase = UnregisterDeviceTokenUseCase(
            deviceRepository = deviceRepository,
            notificationRepository = notificationRepository
        )
    }

    @Nested
    inner class SuccessPath {

        @Test
        fun `returns success when token is unregistered`() = runTest {
            // Given
            coEvery { deviceRepository.getDeviceToken() } returns Result.success(deviceToken)
            coEvery { notificationRepository.unregisterDeviceToken(deviceToken) } returns Unit

            // When
            val result = useCase()

            // Then
            assertTrue(result.isSuccess)
        }

        @Test
        fun `calls unregisterDeviceToken with correct token`() = runTest {
            // Given
            coEvery { deviceRepository.getDeviceToken() } returns Result.success(deviceToken)
            coEvery { notificationRepository.unregisterDeviceToken(deviceToken) } returns Unit

            // When
            useCase()

            // Then
            coVerify(exactly = 1) { notificationRepository.unregisterDeviceToken(deviceToken) }
        }
    }

    @Nested
    inner class FailurePaths {

        @Test
        fun `fails when device token retrieval fails`() = runTest {
            // Given
            val exception = RuntimeException("Token retrieval failed")
            coEvery { deviceRepository.getDeviceToken() } returns Result.failure(exception)

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
            assertEquals("Token retrieval failed", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { notificationRepository.unregisterDeviceToken(any()) }
        }

        @Test
        fun `fails when unregister call throws`() = runTest {
            // Given
            coEvery { deviceRepository.getDeviceToken() } returns Result.success(deviceToken)
            coEvery { notificationRepository.unregisterDeviceToken(deviceToken) } throws
                RuntimeException("Unregister failed")

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
            assertEquals("Unregister failed", result.exceptionOrNull()?.message)
        }
    }
}
