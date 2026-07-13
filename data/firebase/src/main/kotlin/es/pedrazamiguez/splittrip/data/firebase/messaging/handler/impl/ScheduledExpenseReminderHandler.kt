package es.pedrazamiguez.splittrip.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.splittrip.data.firebase.R
import es.pedrazamiguez.splittrip.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.splittrip.domain.constant.NotificationChannelId
import es.pedrazamiguez.splittrip.domain.handler.NotificationHandler
import es.pedrazamiguez.splittrip.domain.model.NotificationContent

class ScheduledExpenseReminderHandler(
    private val context: Context
) : NotificationHandler {

    override fun handle(data: Map<String, String>): NotificationContent {
        val groupId = data["groupId"]
        val expenseId = data["expenseId"]

        return NotificationContent(
            title = context.getString(R.string.notification_scheduled_reminder_title),
            body = context.getString(R.string.notification_scheduled_reminder_body),
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.EXPENSES,
            groupId = groupId,
            notificationId = stableNotificationId("EXPENSE_SCHEDULED", groupId, expenseId)
        )
    }
}
