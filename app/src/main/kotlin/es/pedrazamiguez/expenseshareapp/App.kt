package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.channel.NotificationChannelInitializer
import es.pedrazamiguez.expenseshareapp.di.appModule
import es.pedrazamiguez.expenseshareapp.di.authenticationFeatureModules
import es.pedrazamiguez.expenseshareapp.di.balancesFeatureModules
import es.pedrazamiguez.expenseshareapp.di.contributionsFeatureModules
import es.pedrazamiguez.expenseshareapp.di.coreModules
import es.pedrazamiguez.expenseshareapp.di.currenciesFeatureModules
import es.pedrazamiguez.expenseshareapp.di.dataModules
import es.pedrazamiguez.expenseshareapp.di.expensesFeatureModules
import es.pedrazamiguez.expenseshareapp.di.groupsFeatureModules
import es.pedrazamiguez.expenseshareapp.di.notificationModules
import es.pedrazamiguez.expenseshareapp.di.profileFeatureModules
import es.pedrazamiguez.expenseshareapp.di.settingsFeatureModules
import es.pedrazamiguez.expenseshareapp.di.subunitsFeatureModules
import es.pedrazamiguez.expenseshareapp.di.withdrawalsFeatureModules
import es.pedrazamiguez.expenseshareapp.features.main.di.mainUiModule
import es.pedrazamiguez.expenseshareapp.logging.CrashlyticsTree
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        setupTimber()

        // Create notification channels early so that FCM can auto-display
        // notifications in the system tray even when the app process is dead.
        NotificationChannelInitializer.createChannels(this)

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
                contributionsFeatureModules,
                currenciesFeatureModules,
                expensesFeatureModules,
                groupsFeatureModules,
                subunitsFeatureModules,
                profileFeatureModules,
                settingsFeatureModules,
                withdrawalsFeatureModules
            )
        }
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}
