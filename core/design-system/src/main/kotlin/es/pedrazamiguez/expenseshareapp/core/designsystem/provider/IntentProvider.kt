package es.pedrazamiguez.expenseshareapp.core.designsystem.provider

import android.content.Intent

interface IntentProvider {
    fun getContentIntent(): Intent
    fun getMainIntent(): Intent
    fun getDeepLinkIntent(deepLink: String): Intent
}
