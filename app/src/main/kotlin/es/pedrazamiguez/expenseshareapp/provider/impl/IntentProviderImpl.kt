package es.pedrazamiguez.expenseshareapp.provider.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import es.pedrazamiguez.expenseshareapp.MainActivity
import es.pedrazamiguez.expenseshareapp.core.designsystem.provider.IntentProvider

class IntentProviderImpl(private val context: Context) : IntentProvider {

    override fun getContentIntent(): Intent =
        Intent(context, MainActivity::class.java).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

    override fun getMainIntent(): Intent = Intent(context, MainActivity::class.java)

    override fun getDeepLinkIntent(deepLink: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(deepLink)
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
}
