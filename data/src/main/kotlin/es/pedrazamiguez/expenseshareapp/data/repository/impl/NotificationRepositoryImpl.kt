package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val cloudDataSource: CloudNotificationDataSource
) : NotificationRepository {

    override suspend fun registerDeviceToken(token: String) {
        cloudDataSource.registerDeviceToken(token)
    }

    override suspend fun unregisterDeviceToken(token: String) {
        cloudDataSource.unregisterDeviceToken(token)
    }

}
