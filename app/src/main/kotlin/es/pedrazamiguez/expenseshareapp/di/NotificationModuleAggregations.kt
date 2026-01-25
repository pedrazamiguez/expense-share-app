package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.data.di.notificationsDataModule
import es.pedrazamiguez.expenseshareapp.domain.di.notificationsDomainModule
import org.koin.dsl.module

val notificationModules = module {
    includes(
        notificationsDomainModule, notificationsDataModule
    )
}
