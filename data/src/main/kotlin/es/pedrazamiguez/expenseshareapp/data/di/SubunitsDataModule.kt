package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.SubunitRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudSubunitDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalSubunitDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val subunitsDataModule = module {
    single<SubunitRepository> {
        SubunitRepositoryImpl(
            cloudSubunitDataSource = get<CloudSubunitDataSource>(),
            localSubunitDataSource = get<LocalSubunitDataSource>(),
            authenticationService = get<AuthenticationService>(),
            ioDispatcher = Dispatchers.IO
        )
    }
}
