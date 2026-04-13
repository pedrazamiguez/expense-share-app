package es.pedrazamiguez.splittrip.appcheck

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()

@Suppress("unused") // Called from App.kt (main source set); IDE cannot resolve cross-source-set usages.
internal fun getDebugTokenFromPrefs(context: Context): String? {
    val prefsName = "com.google.firebase.appcheck.debug.store.${FirebaseApp.getInstance().persistenceKey}"
    return context
        .getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        .getString("com.google.firebase.appcheck.debug.DEBUG_SECRET", null)
}
