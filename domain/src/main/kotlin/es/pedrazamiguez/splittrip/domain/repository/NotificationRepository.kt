package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String)
    suspend fun registerDeviceTokenWithRetry(token: String)
    suspend fun unregisterCurrentDevice()
    suspend fun removeStaleDevices()
    suspend fun savePendingToken(token: String)
    suspend fun clearPendingToken()
    fun getPendingTokenFlow(): Flow<String?>
}
