package es.pedrazamiguez.expenseshareapp.core.ui.provider

import android.app.PendingIntent

interface IntentProvider {
    fun getContentIntent(): PendingIntent
}
