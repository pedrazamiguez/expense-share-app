package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val cloudNotificationDataSource: CloudNotificationDataSource
) : NotificationRepository {

    override suspend fun registerDeviceToken(token: String) {
        cloudNotificationDataSource.registerDeviceToken(token)
    }

    override suspend fun unregisterDeviceToken(token: String) {
        cloudNotificationDataSource.unregisterDeviceToken(token)
    }

}
