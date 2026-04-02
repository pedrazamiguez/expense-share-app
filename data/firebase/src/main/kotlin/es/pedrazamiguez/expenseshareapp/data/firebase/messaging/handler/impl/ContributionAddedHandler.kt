package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.formatNotificationAmount
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class ContributionAddedHandler(private val context: Context, private val localeProvider: LocaleProvider) :
    NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val actorName = data["actorName"]
        val amount = formatNotificationAmount(data, localeProvider)
        val groupName = data["groupName"] ?: ""
        val groupId = data["groupId"]

        val body = if (actorName != null) {
            context.getString(
                R.string.notification_contribution_added_body_on_behalf,
                actorName,
                memberName
            )
        } else {
            context.getString(
                R.string.notification_contribution_added_body,
                memberName,
                amount
            )
        }

        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_contribution_added_title)
            },
            body = body,
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.FINANCIAL,
            groupId = groupId,
            notificationId = stableNotificationId("CONTRIBUTION_ADDED", groupId, data["entityId"])
        )
    }
}
