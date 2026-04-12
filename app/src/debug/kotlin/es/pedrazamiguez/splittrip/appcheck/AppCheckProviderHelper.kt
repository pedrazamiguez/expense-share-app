package es.pedrazamiguez.splittrip.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import es.pedrazamiguez.splittrip.BuildConfig

/**
 * Returns a [DebugAppCheckProviderFactory] for debug builds.
 *
 * If a pre-registered debug token is provided via the [BuildConfig.APP_CHECK_DEBUG_TOKEN]
 * field (injected from the `FIREBASE_APP_CHECK_DEBUG_TOKEN` environment variable at build
 * time), it is passed directly to the factory so CI emulators authenticate without
 * generating a new token on every run. On developer machines where the env var is absent
 * the factory falls back to auto-generating a token (printed to Logcat on first use).
 */
internal fun createAppCheckProviderFactory(): AppCheckProviderFactory {
    val ciDebugToken = BuildConfig.APP_CHECK_DEBUG_TOKEN
    return if (ciDebugToken.isNotBlank()) {
        DebugAppCheckProviderFactory(ciDebugToken)
    } else {
        DebugAppCheckProviderFactory.getInstance()
    }
}
