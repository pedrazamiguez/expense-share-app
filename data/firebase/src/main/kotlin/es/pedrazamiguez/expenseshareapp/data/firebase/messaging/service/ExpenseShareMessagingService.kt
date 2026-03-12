package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import es.pedrazamiguez.expenseshareapp.core.designsystem.provider.IntentProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.factory.NotificationHandlerFactory
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationType
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ExpenseShareMessagingService : FirebaseMessagingService(), KoinComponent {

    private val intentProvider: IntentProvider by inject()
    private val notificationHandlerFactory: NotificationHandlerFactory by inject()
    private val notificationRepository: NotificationRepository by inject()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val SUMMARY_ID_OFFSET = 0x10000
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            try {
                notificationRepository.registerDeviceToken(token)
            } catch (e: Exception) {
                Timber.e(e, "Error registering device token")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationType = NotificationType.fromString(remoteMessage.data["type"])
        val handler = notificationHandlerFactory.getHandler(notificationType)
        val content = handler.handle(remoteMessage.data)

        showNotification(content)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun showNotification(content: NotificationContent) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        ensureNotificationChannelsExist(notificationManager)

        val contentIntent = if (!content.deepLink.isNullOrBlank()) {
            intentProvider.getDeepLinkIntent(content.deepLink!!)
        } else {
            intentProvider.getContentIntent()
        }

        val builder = NotificationCompat.Builder(this, content.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Notification grouping by group
        if (!content.groupId.isNullOrBlank()) {
            builder.setGroup(content.groupId)
        }

        notificationManager.notify(content.notificationId, builder.build())

        // Post a summary notification for the group
        if (!content.groupId.isNullOrBlank()) {
            postGroupSummary(notificationManager, content)
        }
    }

    private fun postGroupSummary(
        notificationManager: NotificationManager,
        content: NotificationContent
    ) {
        val summaryNotification = NotificationCompat.Builder(this, content.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(getString(R.string.notification_group_summary, 2))
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(content.title)
            )
            .setGroup(content.groupId)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setContentIntent(intentProvider.getContentIntent())
            .build()

        val summaryId = content.groupId.hashCode() + SUMMARY_ID_OFFSET
        notificationManager.notify(summaryId, summaryNotification)
    }

    private fun ensureNotificationChannelsExist(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    NotificationChannelId.MEMBERSHIP,
                    getString(R.string.notification_channel_membership_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_membership_description)
                },
                NotificationChannel(
                    NotificationChannelId.EXPENSES,
                    getString(R.string.notification_channel_expenses_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_expenses_description)
                },
                NotificationChannel(
                    NotificationChannelId.FINANCIAL,
                    getString(R.string.notification_channel_financial_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_financial_description)
                },
                NotificationChannel(
                    NotificationChannelId.DEFAULT,
                    getString(R.string.notification_channel_expense_updates),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.notification_channel_expense_description)
                }
            )
            manager.createNotificationChannels(channels)
        }
    }

}
