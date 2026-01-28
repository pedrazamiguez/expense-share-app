package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.DeviceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import org.koin.dsl.module

val notificationsDomainModule = module {
    factory<RegisterDeviceTokenUseCase> {
        RegisterDeviceTokenUseCase(
            deviceRepository = get<DeviceRepository>(),
            notificationRepository = get<NotificationRepository>()
        )
    }
}
