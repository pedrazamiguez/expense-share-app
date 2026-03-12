package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class MemberAddedHandler(private val context: Context) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val groupName = data["groupName"] ?: ""
        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_member_added_title)
            },
            body = context.getString(
                R.string.notification_member_added_body,
                memberName
            ),
            deepLink = data["deepLink"]
        )
    }
}

