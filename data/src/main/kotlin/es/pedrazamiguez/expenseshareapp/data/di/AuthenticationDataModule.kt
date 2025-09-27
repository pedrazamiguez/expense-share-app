package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.AuthenticationRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.repository.AuthenticationRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import org.koin.dsl.module

val authenticationDataModule = module {

    single<AuthenticationRepository> { AuthenticationRepositoryImpl(authenticationService = get<AuthenticationService>()) }

}
