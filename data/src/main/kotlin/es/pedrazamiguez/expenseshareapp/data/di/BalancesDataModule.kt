package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.ContributionRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val balancesDataModule = module {
    single<ContributionRepository> {
        ContributionRepositoryImpl(
            cloudContributionDataSource = get<CloudContributionDataSource>(),
            localContributionDataSource = get<LocalContributionDataSource>(),
            authenticationService = get<AuthenticationService>(),
            ioDispatcher = Dispatchers.IO
        )
    }
}