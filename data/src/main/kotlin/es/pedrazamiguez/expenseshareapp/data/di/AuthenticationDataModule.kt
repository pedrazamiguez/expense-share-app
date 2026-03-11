package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.UserRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import org.koin.dsl.module

val authenticationDataModule = module {
    single<UserRepository> {
        UserRepositoryImpl(
            cloudUserDataSource = get<CloudUserDataSource>(),
            localUserDataSource = get<LocalUserDataSource>(),
            authenticationService = get<AuthenticationService>()
        )
    }
}
