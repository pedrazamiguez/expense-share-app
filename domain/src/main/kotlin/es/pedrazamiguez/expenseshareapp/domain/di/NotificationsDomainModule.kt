package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.provider.DeviceTokenProvider
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import org.koin.dsl.module

val notificationsDomainModule = module {
    factory<RegisterDeviceTokenUseCase> {
        RegisterDeviceTokenUseCase(
            deviceTokenProvider = get<DeviceTokenProvider>(),
            notificationRepository = get<NotificationRepository>()
        )
    }
}
