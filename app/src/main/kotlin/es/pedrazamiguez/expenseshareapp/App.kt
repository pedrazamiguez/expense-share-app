package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.data.BuildConfig
import es.pedrazamiguez.expenseshareapp.di.appModule
import es.pedrazamiguez.expenseshareapp.di.authenticationFeatureModules
import es.pedrazamiguez.expenseshareapp.di.balancesFeatureModules
import es.pedrazamiguez.expenseshareapp.di.coreModules
import es.pedrazamiguez.expenseshareapp.di.dataModules
import es.pedrazamiguez.expenseshareapp.di.expensesFeatureModules
import es.pedrazamiguez.expenseshareapp.di.groupsFeatureModules
import es.pedrazamiguez.expenseshareapp.di.notificationModules
import es.pedrazamiguez.expenseshareapp.di.profileFeatureModules
import es.pedrazamiguez.expenseshareapp.di.settingsFeatureModules
import es.pedrazamiguez.expenseshareapp.features.main.di.mainUiModule
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
                coreModules,
                dataModules,

                mainUiModule,
                notificationModules,

                authenticationFeatureModules,
                balancesFeatureModules,
                expensesFeatureModules,
                groupsFeatureModules,
                profileFeatureModules,
                settingsFeatureModules
            )
        }
    }

}
