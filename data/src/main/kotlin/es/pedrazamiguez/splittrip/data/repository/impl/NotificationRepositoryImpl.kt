package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.splittrip.domain.repository.NotificationRepository
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
        Timber.d("registerDeviceToken: acquiring mutex for token=%s…", token.take(10))
        registrationMutex.withLock {
            Timber.d("registerDeviceToken: mutex acquired, writing to Firestore")
            cloudNotificationDataSource.registerDeviceToken(token)
            clearPendingToken()
            Timber.i("registerDeviceToken: SUCCESS — token=%s… registered and pending cleared", token.take(10))
        }

        // Fire-and-forget: clean up stale devices after successful registration
        cleanupScope.launch {
            try {
                Timber.d("registerDeviceToken: starting stale device cleanup")
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
        Timber.d("registerDeviceTokenWithRetry: pending token saved, starting retries for token=%s…", token.take(10))

        var currentDelay = INITIAL_DELAY_MS

        repeat(MAX_RETRIES) { attempt ->
            try {
                registerDeviceToken(token)
                Timber.i("registerDeviceTokenWithRetry: SUCCESS on attempt %d/%d", attempt + 1, MAX_RETRIES)
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

    override suspend fun unregisterCurrentDevice() {
        Timber.d("unregisterCurrentDevice: delegating to cloud data source")
        cloudNotificationDataSource.unregisterCurrentDevice()
        Timber.i("unregisterCurrentDevice: SUCCESS")
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
