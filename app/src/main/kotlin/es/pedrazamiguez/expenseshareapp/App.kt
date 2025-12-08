package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.config.di.coreConfigModule
import es.pedrazamiguez.expenseshareapp.core.ui.di.coreUiModule
import es.pedrazamiguez.expenseshareapp.data.BuildConfig
import es.pedrazamiguez.expenseshareapp.data.di.authenticationDataModule
import es.pedrazamiguez.expenseshareapp.data.di.balancesDataModule
import es.pedrazamiguez.expenseshareapp.data.di.dataCommonModule
import es.pedrazamiguez.expenseshareapp.data.di.expensesDataModule
import es.pedrazamiguez.expenseshareapp.data.di.groupsDataModule
import es.pedrazamiguez.expenseshareapp.data.di.notificationsDataModule
import es.pedrazamiguez.expenseshareapp.data.di.profileDataModule
import es.pedrazamiguez.expenseshareapp.data.di.settingsDataModule
import es.pedrazamiguez.expenseshareapp.data.firebase.di.dataFirebaseModule
import es.pedrazamiguez.expenseshareapp.data.local.di.dataLocalModule
import es.pedrazamiguez.expenseshareapp.data.remote.di.dataRemoteModule
import es.pedrazamiguez.expenseshareapp.di.appModule
import es.pedrazamiguez.expenseshareapp.domain.di.authenticationDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.balancesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.expensesDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.groupsDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.notificationsDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.profileDomainModule
import es.pedrazamiguez.expenseshareapp.domain.di.settingsDomainModule
import es.pedrazamiguez.expenseshareapp.ui.authentication.di.authenticationUiModule
import es.pedrazamiguez.expenseshareapp.ui.balance.di.balancesUiModule
import es.pedrazamiguez.expenseshareapp.ui.expense.di.expensesUiModule
import es.pedrazamiguez.expenseshareapp.ui.group.di.groupsUiModule
import es.pedrazamiguez.expenseshareapp.ui.main.di.mainUiModule
import es.pedrazamiguez.expenseshareapp.ui.profile.di.profileUiModule
import es.pedrazamiguez.expenseshareapp.ui.settings.di.settingsUiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@App)
            modules(
                appModule,

                coreConfigModule,
                coreUiModule,

                dataFirebaseModule,
                dataLocalModule,
                dataRemoteModule,
                dataCommonModule,

                mainUiModule,

                authenticationDomainModule,
                authenticationDataModule,
                authenticationUiModule,

                balancesDomainModule,
                balancesDataModule,
                balancesUiModule,

                expensesDomainModule,
                expensesDataModule,
                expensesUiModule,

                groupsDomainModule,
                groupsDataModule,
                groupsUiModule,

                notificationsDomainModule,
                notificationsDataModule,

                profileDomainModule,
                profileDataModule,
                profileUiModule,

                settingsDomainModule,
                settingsDataModule,
                settingsUiModule,
            )
        }
    }
}
