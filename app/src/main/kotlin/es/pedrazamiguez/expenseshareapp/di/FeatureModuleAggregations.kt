package es.pedrazamiguez.expenseshareapp.di

import es.pedrazamiguez.expenseshareapp.data.di.authenticationDataModule
import es.pedrazamiguez.expenseshareapp.data.di.balancesDataModule
import es.pedrazamiguez.expenseshareapp.data.di.expensesDataModule
import es.pedrazamiguez.expenseshareapp.data.di.groupsDataModule
import es.pedrazamiguez.expenseshareapp.data.di.profileDataModule
import es.pedrazamiguez.expenseshareapp.data.di.settingsDataModule
import es.pedrazamiguez.expenseshareapp.domain.di.authenticationDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.balancesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.currenciesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.expensesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.groupsDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.profileDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.settingsDomainModule
import es.pedrazamiguez.expenseshareapp.features.authentication.di.authenticationUiModule
import es.pedrazamiguez.expenseshareapp.features.balance.di.balancesUiModule
import es.pedrazamiguez.expenseshareapp.features.expense.di.expensesUiModule
import es.pedrazamiguez.expenseshareapp.features.group.di.groupsUiModule
import es.pedrazamiguez.expenseshareapp.features.profile.di.profileUiModule
import es.pedrazamiguez.expenseshareapp.features.settings.di.settingsUiModule
import org.koin.dsl.module

val authenticationFeatureModules = module {
    includes(
        authenticationDomainModule, authenticationDataModule, authenticationUiModule
    )
}

val balancesFeatureModules = module {
    includes(
        balancesDomainModule, balancesDataModule, balancesUiModule
    )
}

val currenciesFeatureModules = module {
    includes(currenciesDomainModule)
}

val expensesFeatureModules = module {
    includes(
        expensesDomainModule, expensesDataModule, expensesUiModule
    )
}

val groupsFeatureModules = module {
    includes(
        groupsDomainModule, groupsDataModule, groupsUiModule
    )
}

val profileFeatureModules = module {
    includes(
        profileDomainModule, profileDataModule, profileUiModule
    )
}

val settingsFeatureModules = module {
    includes(
        settingsDomainModule, settingsDataModule, settingsUiModule
    )
}
