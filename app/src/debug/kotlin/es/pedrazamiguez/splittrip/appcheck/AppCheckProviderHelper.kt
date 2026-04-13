package es.pedrazamiguez.splittrip.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Returns a [DebugAppCheckProviderFactory] for debug builds.
 *
 * The factory auto-generates a debug token on first use and prints it to Logcat.
 * Register that token in the Firebase Console (App Check → Apps → Manage debug tokens)
 * so the emulator / CI device can authenticate.
 *
 * Note: [DebugAppCheckProviderFactory] does not accept a token via its constructor —
 * the SDK manages token matching internally once the token is registered in the console.
 */
internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
