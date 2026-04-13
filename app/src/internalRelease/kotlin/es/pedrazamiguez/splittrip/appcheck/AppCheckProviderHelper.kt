package es.pedrazamiguez.splittrip.appcheck

import android.content.Context
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Returns a [DebugAppCheckProviderFactory] for internal release builds.
 *
 * This variant is used when distributing signed APKs directly (e.g., GitHub Releases)
 * on physical devices without going through Google Play. Play Integrity rejects sideloaded
 * APKs, so the debug provider is used here instead.
 *
 * The factory auto-generates a debug token UUID on first use, persists it in SharedPreferences,
 * and logs it to Logcat. Register that token in:
 *   Firebase Console → App Check → your app → Manage debug tokens
 *
 * Switch to [PlayIntegrityAppCheckProviderFactory] (see the `release` source set) once
 * the app is published on Google Play.
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
