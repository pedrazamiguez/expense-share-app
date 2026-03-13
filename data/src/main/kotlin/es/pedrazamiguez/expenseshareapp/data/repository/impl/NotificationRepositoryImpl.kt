package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationRepositoryImpl(
    private val cloudNotificationDataSource: CloudNotificationDataSource,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository {

    private val cleanupScope = CoroutineScope(ioDispatcher)

    override suspend fun registerDeviceToken(token: String) {
        cloudNotificationDataSource.registerDeviceToken(token)

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

    override suspend fun unregisterDeviceToken(token: String) {
        cloudNotificationDataSource.unregisterDeviceToken(token)
    }

    override suspend fun removeStaleDevices() {
        cloudNotificationDataSource.removeStaleDevices()
    }
}
