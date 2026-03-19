package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.channel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import timber.log.Timber

/**
 * Ensures all notification channels are created at application startup.
 *
 * On Android 8.0+ (API 26), notifications posted to a non-existent channel are
 * **silently dropped**. When the app is killed or in the background, FCM auto-displays
 * notifications via the system tray — `onMessageReceived()` is NOT called. If channels
 * have never been created (fresh install, cleared data), those notifications vanish.
 *
 * Calling [createChannels] from [android.app.Application.onCreate] guarantees channels
 * exist before any FCM message arrives. The operation is idempotent — recreating an
 * existing channel is a no-op (user-modified settings such as importance are preserved).
 */
object NotificationChannelInitializer {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    NotificationChannelId.MEMBERSHIP,
                    context.getString(R.string.notification_channel_membership_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_membership_description)
                },
                NotificationChannel(
                    NotificationChannelId.EXPENSES,
                    context.getString(R.string.notification_channel_expenses_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_expenses_description)
                },
                NotificationChannel(
                    NotificationChannelId.FINANCIAL,
                    context.getString(R.string.notification_channel_financial_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_financial_description)
                },
                NotificationChannel(
                    NotificationChannelId.DEFAULT,
                    context.getString(R.string.notification_channel_expense_updates),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_expense_description)
                }
            )

            manager.createNotificationChannels(channels)
            Timber.d("Notification channels created/verified: %s", channels.map { it.id })
        }
    }
}
