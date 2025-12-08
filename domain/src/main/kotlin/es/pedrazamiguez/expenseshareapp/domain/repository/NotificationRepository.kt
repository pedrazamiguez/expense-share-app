package es.pedrazamiguez.expenseshareapp.domain.repository

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String)
    suspend fun unregisterDeviceToken(token: String)
}
