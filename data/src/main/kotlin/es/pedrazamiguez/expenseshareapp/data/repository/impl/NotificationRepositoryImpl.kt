package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class NotificationRepositoryImpl(
    private val cloudNotificationDataSource: CloudNotificationDataSource,
    private val userPreferences: UserPreferences,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository {

    private val cleanupScope = CoroutineScope(ioDispatcher)
    private val registrationMutex = Mutex()

    companion object {
        internal const val MAX_RETRIES = 4
        internal const val INITIAL_DELAY_MS = 2_000L
        internal const val BACKOFF_MULTIPLIER = 2.0
    }

    override suspend fun registerDeviceToken(token: String) {
        registrationMutex.withLock {
            cloudNotificationDataSource.registerDeviceToken(token)
            clearPendingToken()
        }

        // Fire-and-forget: clean up stale devices after successful registration
        cleanupScope.launch {
            try {
                cloudNotificationDataSource.removeStaleDevices()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Stale device cleanup failed (non-blocking)")
            }
        }
    }

    override suspend fun registerDeviceTokenWithRetry(token: String) {
        // Persist immediately so the token survives process death during retries
        savePendingToken(token)

        var currentDelay = INITIAL_DELAY_MS

        repeat(MAX_RETRIES) { attempt ->
            try {
                registerDeviceToken(token)
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(
                    e,
                    "Token registration attempt ${attempt + 1}/$MAX_RETRIES failed"
                )
                // Only delay between retries, not after the final attempt
                if (attempt < MAX_RETRIES - 1) {
                    Timber.d("Retrying in ${currentDelay}ms")
                    delay(currentDelay)
                    currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
                }
            }
        }

        Timber.e("Token registration failed after $MAX_RETRIES attempts, pending token preserved for later sync")
    }

    override suspend fun unregisterDeviceToken(token: String) {
        cloudNotificationDataSource.unregisterDeviceToken(token)
    }

    override suspend fun removeStaleDevices() {
        cloudNotificationDataSource.removeStaleDevices()
    }

    override suspend fun savePendingToken(token: String) {
        userPreferences.setPendingFcmToken(token)
    }

    override suspend fun clearPendingToken() {
        userPreferences.setPendingFcmToken(null)
    }

    override fun getPendingTokenFlow(): Flow<String?> = userPreferences.pendingFcmToken
}
