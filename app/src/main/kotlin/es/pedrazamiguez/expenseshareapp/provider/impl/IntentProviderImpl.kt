package es.pedrazamiguez.expenseshareapp.provider.impl

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import es.pedrazamiguez.expenseshareapp.MainActivity
import es.pedrazamiguez.expenseshareapp.core.designsystem.provider.IntentProvider

class IntentProviderImpl(private val context: Context) : IntentProvider {

    override fun getContentIntent(): PendingIntent {
        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun getMainIntent(): Intent = Intent(context, MainActivity::class.java)

    override fun getDeepLinkIntent(deepLink: String): PendingIntent {
        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(deepLink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            deepLink.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
