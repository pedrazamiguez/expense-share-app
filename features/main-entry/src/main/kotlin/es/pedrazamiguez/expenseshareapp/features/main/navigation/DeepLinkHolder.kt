package es.pedrazamiguez.expenseshareapp.features.main.navigation

import android.net.Uri

/**
 * Holds a pending deep link URI when the app is cold-started via a notification tap.
 *
 * When the app is killed and a notification is tapped:
 * 1. `MainActivity.onCreate()` receives the deep link intent
 * 2. `AppNavHost` resolves `startDestination` to `Routes.LOGIN` (user not authenticated)
 * 3. The deep link is silently dropped because the NavHost targets `Routes.MAIN`
 *    which doesn't exist in the initial graph yet
 *
 * This holder preserves the deep link so it can be replayed after the auth/onboarding
 * flow completes and the user reaches `Routes.MAIN`.
 *
 * Thread-safe: accessed from main thread only (Activity lifecycle + Compose).
 */
class DeepLinkHolder {

    /**
     * The pending deep link URI to be replayed after auth gate.
     * Set in `MainActivity.onCreate()`, consumed after login/onboarding completion.
     */
    var pendingDeepLink: Uri? = null

    /**
     * Consumes and returns the pending deep link, clearing it to prevent replay.
     *
     * @return The pending URI, or `null` if no deep link is pending.
     */
    fun consumePendingDeepLink(): Uri? {
        val uri = pendingDeepLink
        pendingDeepLink = null
        return uri
    }
}

