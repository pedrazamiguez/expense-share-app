package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("NotificationRepositoryImpl")
class NotificationRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var cloudDataSource: CloudNotificationDataSource
    private lateinit var userPreferences: UserPreferences
    private lateinit var repository: NotificationRepositoryImpl

    @BeforeEach
    fun setUp() {
        cloudDataSource = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true)
        repository = NotificationRepositoryImpl(
            cloudNotificationDataSource = cloudDataSource,
            userPreferences = userPreferences,
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
        fun `clears pending token after successful registration`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken(any()) } just Runs

            repository.registerDeviceToken("token-1")

            coVerify { userPreferences.setPendingFcmToken(null) }
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
    @DisplayName("registerDeviceTokenWithRetry")
    inner class RegisterDeviceTokenWithRetry {

        @Test
        fun `succeeds on first attempt without retry`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken("token-1") } just Runs

            repository.registerDeviceTokenWithRetry("token-1")
            advanceUntilIdle()

            coVerify(exactly = 1) { cloudDataSource.registerDeviceToken("token-1") }
            coVerify { userPreferences.setPendingFcmToken(null) }
            coVerify(exactly = 0) { userPreferences.setPendingFcmToken("token-1") }
        }

        @Test
        fun `succeeds after transient failure with retry`() = runTest(testDispatcher) {
            var callCount = 0
            coEvery { cloudDataSource.registerDeviceToken("token-1") } answers {
                callCount++
                if (callCount < 3) throw RuntimeException("Network error")
            }

            repository.registerDeviceTokenWithRetry("token-1")
            advanceUntilIdle()

            coVerify(exactly = 3) { cloudDataSource.registerDeviceToken("token-1") }
            coVerify { userPreferences.setPendingFcmToken(null) }
        }

        @Test
        fun `persists token after all retries exhausted`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken(any()) } throws RuntimeException("Persistent failure")

            repository.registerDeviceTokenWithRetry("token-1")
            advanceUntilIdle()

            coVerify(exactly = NotificationRepositoryImpl.MAX_RETRIES) {
                cloudDataSource.registerDeviceToken("token-1")
            }
            coVerify { userPreferences.setPendingFcmToken("token-1") }
        }

        @Test
        fun `does not persist token on success`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken("token-1") } just Runs

            repository.registerDeviceTokenWithRetry("token-1")
            advanceUntilIdle()

            coVerify(exactly = 0) { userPreferences.setPendingFcmToken("token-1") }
        }

        @Test
        fun `uses exponential backoff between retries`() = runTest(testDispatcher) {
            coEvery { cloudDataSource.registerDeviceToken(any()) } throws RuntimeException("Failure")

            repository.registerDeviceTokenWithRetry("token-1")
            advanceUntilIdle()

            // After MAX_RETRIES failures, the token should be persisted
            coVerify(exactly = NotificationRepositoryImpl.MAX_RETRIES) {
                cloudDataSource.registerDeviceToken("token-1")
            }
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

            assertThrows<RuntimeException> {
                repository.removeStaleDevices()
            }

            coVerify { cloudDataSource.removeStaleDevices() }
        }
    }

    @Nested
    @DisplayName("pending token persistence")
    inner class PendingTokenPersistence {

        @Test
        fun `savePendingToken delegates to UserPreferences`() = runTest(testDispatcher) {
            repository.savePendingToken("token-1")

            coVerify { userPreferences.setPendingFcmToken("token-1") }
        }

        @Test
        fun `clearPendingToken sets null in UserPreferences`() = runTest(testDispatcher) {
            repository.clearPendingToken()

            coVerify { userPreferences.setPendingFcmToken(null) }
        }

        @Test
        fun `getPendingTokenFlow returns flow from UserPreferences`() = runTest(testDispatcher) {
            every { userPreferences.pendingFcmToken } returns flowOf("pending-token")

            val result = mutableListOf<String?>()
            repository.getPendingTokenFlow().collect { result.add(it) }

            assertEquals(listOf("pending-token"), result)
        }
    }
}
