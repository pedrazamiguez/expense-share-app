package es.pedrazamiguez.splittrip.appcheck

import android.content.Context
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    PlayIntegrityAppCheckProviderFactory.getInstance()

/** Always null in production — Play Integrity does not use debug tokens. */
@Suppress("UnusedParameter")
internal fun getDebugTokenFromPrefs(context: Context): String? = null
