package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.NotificationRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import org.koin.dsl.module

val notificationsDataModule = module {
    single<NotificationRepository> {
        NotificationRepositoryImpl(cloudNotificationDataSource = get<CloudNotificationDataSource>())
    }
}
