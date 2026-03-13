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
import timber.log.Timber

class NotificationRepositoryImpl(
    private val cloudNotificationDataSource: CloudNotificationDataSource,
    private val userPreferences: UserPreferences,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository {

    private val cleanupScope = CoroutineScope(ioDispatcher)

    companion object {
        internal const val MAX_RETRIES = 4
        internal const val INITIAL_DELAY_MS = 2_000L
        internal const val BACKOFF_MULTIPLIER = 2.0
    }

    override suspend fun registerDeviceToken(token: String) {
        cloudNotificationDataSource.registerDeviceToken(token)
        clearPendingToken()

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
        var currentDelay = INITIAL_DELAY_MS
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                registerDeviceToken(token)
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                Timber.w(
                    e,
                    "Token registration attempt ${attempt + 1}/$MAX_RETRIES failed, retrying in ${currentDelay}ms"
                )
                delay(currentDelay)
                currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
            }
        }

        // All retries exhausted — persist the token for recovery on next app start
        Timber.e(lastException, "Token registration failed after $MAX_RETRIES attempts, persisting for later sync")
        savePendingToken(token)
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

    override fun getPendingTokenFlow(): Flow<String?> {
        return userPreferences.pendingFcmToken
    }
}
