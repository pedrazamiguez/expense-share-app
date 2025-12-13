package es.pedrazamiguez.expenseshareapp.features.authentication.di

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.viewmodel.AuthenticationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authenticationUiModule = module {
    viewModel {
        AuthenticationViewModel(
            authenticationService = get<AuthenticationService>(),
            registerDeviceTokenUseCase = get<RegisterDeviceTokenUseCase>()
        )
    }
}
