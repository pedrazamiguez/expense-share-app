package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
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
        private const val SHORTCUT_ID = "expense_share_updates_shortcut"
        private const val REQUEST_CODE_BUBBLE = 1
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

    private fun showNotification(content: NotificationContent) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        ensureNotificationChannelExists(notificationManager)

        val person = createPerson(content.title)
        publishDynamicShortcut(person, intentProvider.getMainIntent())

        val bubbleMetadata = createBubbleMetadata()

        val notification = buildNotification(content, person, bubbleMetadata)

        notificationManager.notify(content.hashCode(), notification)
    }

    private fun ensureNotificationChannelExists(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_expense_updates),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_expense_description)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(true)
                }
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun createPerson(name: String): Person {
        return Person.Builder()
            .setName(name)
            .setIcon(IconCompat.createWithResource(this, android.R.drawable.ic_dialog_info))
            .setImportant(true)
            .build()
    }

    private fun publishDynamicShortcut(person: Person, targetIntent: Intent) {
        val shortcutIntent = targetIntent.apply {
            action = Intent.ACTION_VIEW
        }

        val shortcut = ShortcutInfoCompat.Builder(this, SHORTCUT_ID)
            .setLongLived(true)
            .setIntent(shortcutIntent)
            .setShortLabel(person.name ?: "Chat")
            .setIcon(person.icon)
            .setPerson(person)
            .addCapabilityBinding("actions.intent.CREATE_MESSAGE")
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }

    private fun createBubbleMetadata(): NotificationCompat.BubbleMetadata {
        val targetIntent = intentProvider.getMainIntent()
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val bubbleIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE_BUBBLE,
            targetIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val icon = IconCompat.createWithResource(this, android.R.drawable.ic_dialog_info)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            NotificationCompat.BubbleMetadata.Builder(SHORTCUT_ID)
                .setDesiredHeight(600)
                .setAutoExpandBubble(true)
                .setSuppressNotification(false)
                .build()
        } else {
            NotificationCompat.BubbleMetadata.Builder(bubbleIntent, icon)
                .setDesiredHeight(600)
                .setAutoExpandBubble(true)
                .setSuppressNotification(false)
                .build()
        }

    }

    private fun buildNotification(
        content: NotificationContent,
        person: Person,
        bubbleMetadata: NotificationCompat.BubbleMetadata
    ): Notification {
        val pendingIntent = intentProvider.getContentIntent()

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.MessagingStyle(person)
                    .addMessage(
                        NotificationCompat.MessagingStyle.Message(
                            content.body, System.currentTimeMillis(), person
                        )
                    )
            )
            .setBubbleMetadata(bubbleMetadata)
            .setShortcutId(SHORTCUT_ID)
            .addPerson(person)
            .build()
    }

}
