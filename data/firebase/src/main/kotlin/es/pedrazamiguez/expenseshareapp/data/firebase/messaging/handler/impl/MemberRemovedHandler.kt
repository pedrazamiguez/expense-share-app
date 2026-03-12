package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class MemberRemovedHandler(private val context: Context) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val groupName = data["groupName"] ?: ""
        val groupId = data["groupId"]
        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_member_removed_title)
            },
            body = context.getString(
                R.string.notification_member_removed_body,
                memberName
            ),
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.MEMBERSHIP,
            groupId = groupId,
            notificationId = stableNotificationId("MEMBER_REMOVED", groupId, data["entityId"])
        )
    }
}

