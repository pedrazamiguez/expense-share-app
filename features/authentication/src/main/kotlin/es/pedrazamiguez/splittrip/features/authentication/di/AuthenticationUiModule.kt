package es.pedrazamiguez.splittrip.features.authentication.di

import es.pedrazamiguez.splittrip.domain.usecase.auth.SignInWithEmailUseCase
import es.pedrazamiguez.splittrip.domain.usecase.auth.SignInWithGoogleUseCase
import es.pedrazamiguez.splittrip.features.authentication.presentation.viewmodel.AuthenticationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authenticationUiModule = module {
    viewModel {
        AuthenticationViewModel(
            signInWithEmailUseCase = get<SignInWithEmailUseCase>(),
            signInWithGoogleUseCase = get<SignInWithGoogleUseCase>()
        )
    }
}
