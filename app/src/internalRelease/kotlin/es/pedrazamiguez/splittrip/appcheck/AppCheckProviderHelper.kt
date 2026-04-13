package es.pedrazamiguez.splittrip.appcheck

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import es.pedrazamiguez.splittrip.BuildConfig

internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()

@Suppress("unused")
internal fun seedDebugToken(context: Context) {
    val persistenceKey = FirebaseApp.getInstance().persistenceKey
    val prefsName = "com.google.firebase.appcheck.debug.store.$persistenceKey"
    context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        .edit()
        .putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", BuildConfig.APP_CHECK_DEBUG_TOKEN)
        .apply()
}

@Suppress("unused")
internal fun getDebugTokenFromPrefs(context: Context): String? {
    val prefsName = "com.google.firebase.appcheck.debug.store.${FirebaseApp.getInstance().persistenceKey}"
    return context
        .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        .getString("com.google.firebase.appcheck.debug.DEBUG_SECRET", null)
}
