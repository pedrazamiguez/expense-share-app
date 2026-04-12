package es.pedrazamiguez.splittrip

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import es.pedrazamiguez.splittrip.appcheck.createAppCheckProviderFactory
import es.pedrazamiguez.splittrip.data.firebase.messaging.channel.NotificationChannelInitializer
import es.pedrazamiguez.splittrip.di.appModule
import es.pedrazamiguez.splittrip.di.authenticationFeatureModules
import es.pedrazamiguez.splittrip.di.balancesFeatureModules
import es.pedrazamiguez.splittrip.di.contributionsFeatureModules
import es.pedrazamiguez.splittrip.di.coreModules
import es.pedrazamiguez.splittrip.di.currenciesFeatureModules
import es.pedrazamiguez.splittrip.di.dataModules
import es.pedrazamiguez.splittrip.di.expensesFeatureModules
import es.pedrazamiguez.splittrip.di.groupsFeatureModules
import es.pedrazamiguez.splittrip.di.notificationModules
import es.pedrazamiguez.splittrip.di.profileFeatureModules
import es.pedrazamiguez.splittrip.di.settingsFeatureModules
import es.pedrazamiguez.splittrip.di.subunitsFeatureModules
import es.pedrazamiguez.splittrip.di.withdrawalsFeatureModules
import es.pedrazamiguez.splittrip.features.main.di.mainUiModule
import es.pedrazamiguez.splittrip.logging.CrashlyticsTree
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        setupTimber()

        // Firebase App Check must be installed before any Firebase SDK is used.
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(createAppCheckProviderFactory())

        // In debug builds, proactively request an App Check token so the debug
        // secret is printed to Logcat immediately on startup — no Firebase call
        // required. Register that token in:
        //   Firebase Console → App Check → your app → Manage debug tokens
        if (BuildConfig.DEBUG) {
            FirebaseAppCheck.getInstance()
                .getAppCheckToken(false)
                .addOnSuccessListener {
                    Timber.d("App Check: debug token printed above — register it in Firebase Console")
                }
        }

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
