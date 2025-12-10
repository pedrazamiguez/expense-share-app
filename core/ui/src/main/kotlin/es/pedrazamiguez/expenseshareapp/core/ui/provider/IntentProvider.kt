package es.pedrazamiguez.expenseshareapp.core.ui.provider

import android.app.PendingIntent
import android.content.Intent

interface IntentProvider {
    fun getContentIntent(): PendingIntent
    fun getMainIntent(): Intent
}
