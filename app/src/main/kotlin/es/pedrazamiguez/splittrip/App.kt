package es.pedrazamiguez.splittrip

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.google.firebase.appcheck.FirebaseAppCheck
import es.pedrazamiguez.splittrip.appcheck.createAppCheckProviderFactory
import es.pedrazamiguez.splittrip.appcheck.getDebugTokenFromPrefs
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

        // Firebase App Check must be installed before any Firebase SDK is used.
        // CrashlyticsTree calls FirebaseCrashlytics.getInstance() at construction
        // time, so setupTimber() must come after App Check is installed.
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(createAppCheckProviderFactory())

        setupTimber()

        // In debug and internalRelease builds, proactively request an App Check token so the
        // debug secret is printed to Logcat immediately on startup — no Firestore, Auth, or other
        // Firebase product call is required. Register that token in:
        //   Firebase Console → App Check → your app → Manage debug tokens
        // NOTE: Firebase takes up to 5 minutes to propagate a newly registered token. If you see
        // "App Check: token exchange FAILED" below, wait a few minutes, kill the app, and relaunch.
        if (BuildConfig.USE_DEBUG_APP_CHECK) {
            FirebaseAppCheck.getInstance()
                .getAppCheckToken(false)
                .addOnSuccessListener {
                    val token = getDebugTokenFromPrefs(applicationContext)
                    Timber.d("App Check: token obtained successfully ✓ (registered debug token: $token)")
                }
                .addOnFailureListener { e ->
                    val token = getDebugTokenFromPrefs(applicationContext)
                    Timber.e(
                        e,
                        "App Check: token exchange FAILED — register this debug token in " +
                            "Firebase Console (App Check → your app → Manage debug tokens): $token"
                    )
                    // Copy token to clipboard and show Toast so it's accessible without Logcat
                    // (useful when testing on a physical device without ADB).
                    if (token != null) {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("App Check debug token", token))
                        Toast.makeText(
                            applicationContext,
                            "App Check FAILED ✗\nDebug token copied to clipboard:\n$token\n" +
                                "→ Register it in Firebase Console",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
        // debug: Logcat only.
        // internalRelease: Logcat (for device debugging) + Crashlytics.
        // release: Crashlytics only (no Logcat).
        if (BuildConfig.USE_DEBUG_APP_CHECK) {
            Timber.plant(Timber.DebugTree())
        }
        if (!BuildConfig.DEBUG) {
            Timber.plant(CrashlyticsTree())
        }
    }
}
