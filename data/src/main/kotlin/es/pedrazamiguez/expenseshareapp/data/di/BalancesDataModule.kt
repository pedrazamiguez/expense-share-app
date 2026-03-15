package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.CashWithdrawalRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.repository.impl.ContributionRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val balancesDataModule = module {
    single<ContributionRepository> {
        ContributionRepositoryImpl(
            cloudContributionDataSource = get<CloudContributionDataSource>(),
            localContributionDataSource = get<LocalContributionDataSource>(),
            authenticationService = get<AuthenticationService>()
        )
    }

    single<CashWithdrawalRepository> {
        CashWithdrawalRepositoryImpl(
            cloudCashWithdrawalDataSource = get<CloudCashWithdrawalDataSource>(),
            localCashWithdrawalDataSource = get<LocalCashWithdrawalDataSource>(),
            authenticationService = get<AuthenticationService>(),
            ioDispatcher = Dispatchers.IO
        )
    }
}
