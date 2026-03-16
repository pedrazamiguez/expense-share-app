package es.pedrazamiguez.expenseshareapp.core.designsystem.provider

import android.content.Intent

interface IntentProvider {
    val targetActivityClassName: String
    fun getMainIntent(): Intent
}
