package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("NotificationRepositoryImpl")
class NotificationRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var cloudDataSource: CloudNotificationDataSource
    private lateinit var repository: NotificationRepositoryImpl

    @BeforeEach
    fun setUp() {
        cloudDataSource = mockk(relaxed = true)
        repository = NotificationRepositoryImpl(
            cloudNotificationDataSource = cloudDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @Nested
    @DisplayName("registerDeviceToken")
    inner class RegisterDeviceToken {

        @Test
        fun `delegates registration to cloud data source`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken("token-1") } just Runs

            repository.registerDeviceToken("token-1")

            coVerify { cloudDataSource.registerDeviceToken("token-1") }
        }

        @Test
        fun `triggers fire-and-forget stale cleanup after registration`() =
            runTest(testDispatcher) {
                coEvery { cloudDataSource.registerDeviceToken(any()) } just Runs
                coEvery { cloudDataSource.removeStaleDevices() } just Runs

                repository.registerDeviceToken("token-1")
                advanceUntilIdle()

                coVerify(exactly = 1) { cloudDataSource.removeStaleDevices() }
            }

        @Test
        fun `stale cleanup failure does not propagate`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken(any()) } just Runs
            coEvery { cloudDataSource.removeStaleDevices() } throws RuntimeException("Cleanup failed")

            repository.registerDeviceToken("token-1")
            advanceUntilIdle()

            // Registration succeeded, cleanup failure was caught silently
            coVerify { cloudDataSource.registerDeviceToken("token-1") }
            coVerify { cloudDataSource.removeStaleDevices() }
        }
    }

    @Nested
    @DisplayName("unregisterDeviceToken")
    inner class UnregisterDeviceToken {

        @Test
        fun `delegates to cloud data source`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.unregisterDeviceToken("token-1") } just Runs

            repository.unregisterDeviceToken("token-1")

            coVerify { cloudDataSource.unregisterDeviceToken("token-1") }
        }
    }

    @Nested
    @DisplayName("removeStaleDevices")
    inner class RemoveStaleDevices {

        @Test
        fun `delegates to cloud data source`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.removeStaleDevices() } just Runs

            repository.removeStaleDevices()

            coVerify { cloudDataSource.removeStaleDevices() }
        }

        @Test
        fun `propagates exceptions on direct invocation`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.removeStaleDevices() } throws RuntimeException("Error")

            try {
                repository.removeStaleDevices()
            } catch (e: RuntimeException) {
                // Expected — direct invocations should propagate
            }

            coVerify { cloudDataSource.removeStaleDevices() }
        }
    }
}

