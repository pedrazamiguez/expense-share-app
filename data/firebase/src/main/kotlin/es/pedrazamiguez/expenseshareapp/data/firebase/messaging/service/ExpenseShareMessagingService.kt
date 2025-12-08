package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import es.pedrazamiguez.expenseshareapp.core.ui.provider.IntentProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.factory.NotificationHandlerFactory
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
        private const val NOTIFICATION_CHANNEL_ID = "expense_share_updates"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            try {
                notificationRepository.registerDeviceToken(token)
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "Error registering device token"
                )
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationType = NotificationType.fromString(remoteMessage.data["type"])
        val handler = notificationHandlerFactory.getHandler(notificationType)
        val content = handler.handle(remoteMessage.data)

        showNotification(content)
    }

    private fun showNotification(content: NotificationContent) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_expense_updates),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = intentProvider.getContentIntent()

        val notificationBuilder = NotificationCompat
            .Builder(
                this,
                NOTIFICATION_CHANNEL_ID
            )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = System
            .currentTimeMillis()
            .toInt()

        notificationManager.notify(
            notificationId,
            notificationBuilder.build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
