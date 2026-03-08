package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithGoogleUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import org.koin.dsl.module

val authenticationDomainModule = module {
    factory {
        SignInWithGoogleUseCase(
            authenticationService = get<AuthenticationService>(),
            userRepository = get<UserRepository>(),
            registerDeviceTokenUseCase = get<RegisterDeviceTokenUseCase>()
        )
    }
}
