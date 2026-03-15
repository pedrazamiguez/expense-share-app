package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class MemberAddedHandler(private val context: Context) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val actorName = data["actorName"]
        val groupName = data["groupName"] ?: ""
        val groupId = data["groupId"]

        val body = if (actorName != null) {
            context.getString(
                R.string.notification_member_added_by_admin_body,
                actorName,
                memberName
            )
        } else {
            context.getString(
                R.string.notification_member_added_body,
                memberName
            )
        }

        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_member_added_title)
            },
            body = body,
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.MEMBERSHIP,
            groupId = groupId,
            notificationId = stableNotificationId("MEMBER_ADDED", groupId, data["entityId"])
        )
    }
}
