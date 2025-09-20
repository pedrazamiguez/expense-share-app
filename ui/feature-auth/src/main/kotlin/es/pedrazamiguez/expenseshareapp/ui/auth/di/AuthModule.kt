package es.pedrazamiguez.expenseshareapp.ui.auth.di

import es.pedrazamiguez.expenseshareapp.domain.repository.AuthRepository
import es.pedrazamiguez.expenseshareapp.ui.auth.presentation.viewmodel.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel { AuthViewModel(authRepository = get<AuthRepository>()) }
}
