package es.pedrazamiguez.splittrip.data.di

import es.pedrazamiguez.splittrip.data.local.datastore.NotificationUserPreferences
import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.data.repository.impl.NotificationPreferencesRepositoryImpl
import es.pedrazamiguez.splittrip.data.repository.impl.NotificationRepositoryImpl
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.splittrip.domain.repository.NotificationPreferencesRepository
import es.pedrazamiguez.splittrip.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val notificationsDataModule = module {
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            cloudNotificationDataSource = get<CloudNotificationDataSource>(),
            userPreferences = get<UserPreferences>(),
            ioDispatcher = Dispatchers.IO
        )
    }

    single<NotificationPreferencesRepository> {
        NotificationPreferencesRepositoryImpl(
            notificationUserPreferences = get<NotificationUserPreferences>()
        )
    }
}
