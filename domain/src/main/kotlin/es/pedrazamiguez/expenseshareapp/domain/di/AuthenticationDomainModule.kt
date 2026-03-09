package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithEmailUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithGoogleUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
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
}
