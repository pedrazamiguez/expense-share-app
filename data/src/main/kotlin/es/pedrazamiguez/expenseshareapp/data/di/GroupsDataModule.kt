package es.pedrazamiguez.expenseshareapp.data.di

import es.pedrazamiguez.expenseshareapp.data.repository.impl.GroupRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import org.koin.dsl.module

val groupsDataModule = module {
    single<GroupRepository> {
        GroupRepositoryImpl(
            cloudGroupDataSource = get<CloudGroupDataSource>(),
            localGroupDataSource = get<LocalGroupDataSource>(),
            cloudExpenseDataSource = get<CloudExpenseDataSource>(),
            localExpenseDataSource = get<LocalExpenseDataSource>(),
            cloudContributionDataSource = get<CloudContributionDataSource>(),
            localContributionDataSource = get<LocalContributionDataSource>(),
            cloudCashWithdrawalDataSource = get<CloudCashWithdrawalDataSource>(),
            localCashWithdrawalDataSource = get<LocalCashWithdrawalDataSource>()
        )
    }
}
