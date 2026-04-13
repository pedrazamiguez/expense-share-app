package es.pedrazamiguez.splittrip.appcheck

import android.content.Context
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

internal fun createAppCheckProviderFactory(): AppCheckProviderFactory =
    PlayIntegrityAppCheckProviderFactory.getInstance()

@Suppress("UnusedParameter")
internal fun seedDebugToken(context: Context) = Unit

@Suppress("UnusedParameter")
internal fun getDebugTokenFromPrefs(context: Context): String? = null
