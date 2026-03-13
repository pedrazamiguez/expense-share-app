package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SyncPendingTokenUseCase")
class SyncPendingTokenUseCaseTest {

    private lateinit var notificationRepository: NotificationRepository
    private lateinit var useCase: SyncPendingTokenUseCase

    @BeforeEach
    fun setUp() {
        notificationRepository = mockk()
        useCase = SyncPendingTokenUseCase(
            notificationRepository = notificationRepository
        )
    }

    @Nested
    @DisplayName("when pending token exists")
    inner class PendingTokenExists {

        @Test
        fun `registers the pending token with cloud`() = runTest {
            every { notificationRepository.getPendingTokenFlow() } returns flowOf("pending-token")
            coEvery { notificationRepository.registerDeviceToken("pending-token") } just Runs

            val result = useCase()

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { notificationRepository.registerDeviceToken("pending-token") }
        }

        @Test
        fun `returns failure when registration throws`() = runTest {
            every { notificationRepository.getPendingTokenFlow() } returns flowOf("pending-token")
            coEvery {
                notificationRepository.registerDeviceToken("pending-token")
            } throws RuntimeException("Network error")

            val result = useCase()

            assertTrue(result.isFailure)
        }
    }

    @Nested
    @DisplayName("when no pending token")
    inner class NoPendingToken {

        @Test
        fun `returns success without calling registerDeviceToken`() = runTest {
            every { notificationRepository.getPendingTokenFlow() } returns flowOf(null)

            val result = useCase()

            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { notificationRepository.registerDeviceToken(any()) }
        }
    }
}

