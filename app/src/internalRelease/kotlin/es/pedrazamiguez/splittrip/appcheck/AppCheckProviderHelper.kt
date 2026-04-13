package es.pedrazamiguez.splittrip.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Returns a [DebugAppCheckProviderFactory] for internal release builds.
 *
 * This variant is used when distributing signed APKs directly (e.g., GitHub Releases)
 * on physical devices without going through Google Play. Play Integrity rejects sideloaded
 * APKs, so the debug provider is used here instead.
 *
 * The factory auto-generates a debug token on first use and prints it to Logcat.
 * Register that token in the Firebase Console (App Check → Apps → Manage debug tokens).
 *
 * Switch to [PlayIntegrityAppCheckProviderFactory] (see the `release` source set) once
 * the app is published on Google Play.
 */
internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
