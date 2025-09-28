package es.pedrazamiguez.expenseshareapp.ui.authentication.di

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.viewmodel.AuthenticationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authenticationUiModule = module {
    viewModel { AuthenticationViewModel(authenticationService = get<AuthenticationService>()) }
}
