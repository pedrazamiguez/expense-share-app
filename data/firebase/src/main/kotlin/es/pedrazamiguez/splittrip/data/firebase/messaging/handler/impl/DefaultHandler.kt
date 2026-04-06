package es.pedrazamiguez.splittrip.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.splittrip.data.firebase.R
import es.pedrazamiguez.splittrip.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.splittrip.domain.constant.NotificationChannelId
import es.pedrazamiguez.splittrip.domain.handler.NotificationHandler
import es.pedrazamiguez.splittrip.domain.model.NotificationContent

class DefaultHandler(private val context: Context) : NotificationHandler {

    override fun handle(data: Map<String, String>): NotificationContent {
        val title = data["title"] ?: context.getString(R.string.notification_default_title)
        val body = data["body"] ?: context.getString(R.string.notification_default_body)
        val groupId = data["groupId"]
        return NotificationContent(
            title = title,
            body = body,
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.DEFAULT,
            groupId = groupId,
            notificationId = stableNotificationId("DEFAULT", groupId, data["entityId"])
        )
    }
}
