package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.data.repository.impl.NotificationPreferencesRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.repository.impl.NotificationRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationPreferencesRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import org.koin.dsl.module

val notificationsDataModule = module {
    single<NotificationRepository> {
        NotificationRepositoryImpl(cloudNotificationDataSource = get<CloudNotificationDataSource>())
    }

    single<NotificationPreferencesRepository> {
        NotificationPreferencesRepositoryImpl(userPreferences = get<UserPreferences>())
    }
}
