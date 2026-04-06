package es.pedrazamiguez.splittrip.di

import es.pedrazamiguez.splittrip.data.di.authenticationDataModule
import es.pedrazamiguez.splittrip.data.di.balancesDataModule
import es.pedrazamiguez.splittrip.data.di.expensesDataModule
import es.pedrazamiguez.splittrip.data.di.groupsDataModule
import es.pedrazamiguez.splittrip.data.di.profileDataModule
import es.pedrazamiguez.splittrip.data.di.settingsDataModule
import es.pedrazamiguez.splittrip.data.di.subunitsDataModule
import es.pedrazamiguez.splittrip.domain.di.authenticationDomainModule
import es.pedrazamiguez.splittrip.domain.di.balancesDomainModule
import es.pedrazamiguez.splittrip.domain.di.contributionsDomainModule
import es.pedrazamiguez.splittrip.domain.di.currenciesDomainModule
import es.pedrazamiguez.splittrip.domain.di.expensesDomainModule
import es.pedrazamiguez.splittrip.domain.di.groupsDomainModule
import es.pedrazamiguez.splittrip.domain.di.profileDomainModule
import es.pedrazamiguez.splittrip.domain.di.settingsDomainModule
import es.pedrazamiguez.splittrip.domain.di.subunitsDomainModule
import es.pedrazamiguez.splittrip.domain.di.withdrawalsDomainModule
import es.pedrazamiguez.splittrip.features.authentication.di.authenticationUiModule
import es.pedrazamiguez.splittrip.features.balance.di.balancesUiModule
import es.pedrazamiguez.splittrip.features.contribution.di.contributionsUiModule
import es.pedrazamiguez.splittrip.features.expense.di.expensesUiModule
import es.pedrazamiguez.splittrip.features.group.di.groupsUiModule
import es.pedrazamiguez.splittrip.features.profile.di.profileUiModule
import es.pedrazamiguez.splittrip.features.settings.di.settingsUiModule
import es.pedrazamiguez.splittrip.features.subunit.di.subunitsUiModule
import es.pedrazamiguez.splittrip.features.withdrawal.di.withdrawalsUiModule
import org.koin.dsl.module

val authenticationFeatureModules = module {
    includes(
        authenticationDomainModule,
        authenticationDataModule,
        authenticationUiModule
    )
}

val balancesFeatureModules = module {
    includes(
        balancesDomainModule,
        balancesDataModule,
        balancesUiModule
    )
}

val contributionsFeatureModules = module {
    includes(
        contributionsDomainModule,
        contributionsUiModule
    )
}

val currenciesFeatureModules = module {
    includes(currenciesDomainModule)
}

val expensesFeatureModules = module {
    includes(
        expensesDomainModule,
        expensesDataModule,
        expensesUiModule
    )
}

val groupsFeatureModules = module {
    includes(
        groupsDomainModule,
        groupsDataModule,
        groupsUiModule
    )
}

val subunitsFeatureModules = module {
    includes(
        subunitsDomainModule,
        subunitsDataModule,
        subunitsUiModule
    )
}

val profileFeatureModules = module {
    includes(
        profileDomainModule,
        profileDataModule,
        profileUiModule
    )
}

val settingsFeatureModules = module {
    includes(
        settingsDomainModule,
        settingsDataModule,
        settingsUiModule
    )
}

val withdrawalsFeatureModules = module {
    includes(
        withdrawalsDomainModule,
        withdrawalsUiModule
    )
}
