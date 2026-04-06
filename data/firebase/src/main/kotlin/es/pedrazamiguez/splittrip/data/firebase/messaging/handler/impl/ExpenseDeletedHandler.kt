package es.pedrazamiguez.splittrip.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.data.firebase.R
import es.pedrazamiguez.splittrip.data.firebase.messaging.handler.formatNotificationAmount
import es.pedrazamiguez.splittrip.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.splittrip.domain.constant.NotificationChannelId
import es.pedrazamiguez.splittrip.domain.handler.NotificationHandler
import es.pedrazamiguez.splittrip.domain.model.NotificationContent

class ExpenseDeletedHandler(private val context: Context, private val localeProvider: LocaleProvider) :
    NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val amount = formatNotificationAmount(data, localeProvider)
        val groupName = data["groupName"] ?: ""
        val groupId = data["groupId"]
        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_expense_deleted_title)
            },
            body = context.getString(
                R.string.notification_expense_deleted_body,
                memberName,
                amount
            ),
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.EXPENSES,
            groupId = groupId,
            notificationId = stableNotificationId("EXPENSE_DELETED", groupId, data["entityId"])
        )
    }
}
