package es.pedrazamiguez.splittrip.appcheck

import android.content.Context
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Returns a [DebugAppCheckProviderFactory] for debug builds.
 *
 * The factory auto-generates a debug token UUID on first use, persists it in SharedPreferences,
 * and logs it to Logcat. Register that token in:
 *   Firebase Console → App Check → your app → Manage debug tokens
 *
 * Note: [DebugAppCheckProviderFactory] does not accept a token via its constructor —
 * the SDK manages token matching internally once the token is registered in the console.
 */
internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()

/**
 * Reads the debug token UUID that [DebugAppCheckProviderFactory] generated and persisted.
 *
 * Must be called AFTER [com.google.firebase.appcheck.FirebaseAppCheck.getAppCheckToken] to ensure
 * the SDK has already generated and stored the UUID in SharedPreferences.
 *
 * NOTE: This reads from Firebase App Check Debug SDK's internal SharedPreferences (key names
 * known from its source code). If Firebase changes those keys in a future version, this
 * returns null gracefully — the in-app token display simply shows nothing, no crash.
 */
@Suppress("unused") // Called from App.kt (main source set); IDE cannot resolve cross-source-set usages.
internal fun getDebugTokenFromPrefs(context: Context): String? =
    context
        .getSharedPreferences("FirebaseAppCheckDebugProvider", Context.MODE_PRIVATE)
        .getString("firebase_app_check_debug_token", null)
