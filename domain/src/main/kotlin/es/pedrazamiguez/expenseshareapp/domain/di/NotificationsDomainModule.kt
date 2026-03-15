package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.DeviceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationPreferencesRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.GetNotificationPreferencesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UnregisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UpdateNotificationPreferenceUseCase
import org.koin.dsl.module

val notificationsDomainModule = module {
    factory<RegisterDeviceTokenUseCase> {
        RegisterDeviceTokenUseCase(
            deviceRepository = get<DeviceRepository>(),
            notificationRepository = get<NotificationRepository>()
        )
    }

    factory<UnregisterDeviceTokenUseCase> {
        UnregisterDeviceTokenUseCase(
            notificationRepository = get<NotificationRepository>()
        )
    }

    factory<GetNotificationPreferencesUseCase> {
        GetNotificationPreferencesUseCase(
            repository = get<NotificationPreferencesRepository>()
        )
    }

    factory<UpdateNotificationPreferenceUseCase> {
        UpdateNotificationPreferenceUseCase(
            repository = get<NotificationPreferencesRepository>()
        )
    }
}
