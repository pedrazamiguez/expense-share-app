package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.config.di.coreConfigModule
import es.pedrazamiguez.expenseshareapp.data.di.balancesDataModule
import es.pedrazamiguez.expenseshareapp.data.di.currencyDataModule
import es.pedrazamiguez.expenseshareapp.data.di.dataModule
import es.pedrazamiguez.expenseshareapp.data.di.expensesDataModule
import es.pedrazamiguez.expenseshareapp.data.di.groupsDataModule
import es.pedrazamiguez.expenseshareapp.data.di.profileDataModule
import es.pedrazamiguez.expenseshareapp.domain.di.balancesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.expensesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.groupsDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.profileDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.useCaseModule
import es.pedrazamiguez.expenseshareapp.ui.auth.di.authModule
import es.pedrazamiguez.expenseshareapp.ui.balance.di.balancesUiModule
import es.pedrazamiguez.expenseshareapp.ui.expense.di.expensesUiModule
import es.pedrazamiguez.expenseshareapp.ui.group.di.groupsUiModule
import es.pedrazamiguez.expenseshareapp.ui.profile.di.profileUiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                coreConfigModule,
                dataModule,
                useCaseModule,
                currencyDataModule,
                authModule,

                balancesDomainModule,
                balancesDataModule,
                balancesUiModule,

                expensesDomainModule,
                expensesDataModule,
                expensesUiModule,

                groupsDomainModule,
                groupsDataModule,
                groupsUiModule,

                profileDomainModule,
                profileDataModule,
                profileUiModule,
            )
        }
    }
}
