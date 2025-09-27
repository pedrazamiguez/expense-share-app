package es.pedrazamiguez.expenseshareapp.ui.authentication.di

import es.pedrazamiguez.expenseshareapp.domain.repository.AuthenticationRepository
import es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.viewmodel.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authenticationUiModule = module {
    viewModel { AuthViewModel(authenticationRepository = get<AuthenticationRepository>()) }
}
