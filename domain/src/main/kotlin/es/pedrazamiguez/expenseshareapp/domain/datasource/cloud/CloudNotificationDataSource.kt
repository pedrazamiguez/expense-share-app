package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

interface CloudNotificationDataSource {
    suspend fun registerDeviceToken(token: String)
    suspend fun unregisterDeviceToken(token: String)
}
