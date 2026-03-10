package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleaner
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithEmailUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithGoogleUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignOutUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UnregisterDeviceTokenUseCase
import org.koin.dsl.module

val authenticationDomainModule = module {
    factory {
        SignInWithEmailUseCase(
            authenticationService = get<AuthenticationService>(),
            registerDeviceTokenUseCase = get<RegisterDeviceTokenUseCase>()
        )
    }
    factory {
        SignInWithGoogleUseCase(
            authenticationService = get<AuthenticationService>(),
            registerDeviceTokenUseCase = get<RegisterDeviceTokenUseCase>()
        )
    }
    factory {
        SignOutUseCase(
            unregisterDeviceTokenUseCase = get<UnregisterDeviceTokenUseCase>(),
            preferenceRepository = get<PreferenceRepository>(),
            localDatabaseCleaner = get<LocalDatabaseCleaner>(),
            authenticationService = get<AuthenticationService>()
        )
    }
}
