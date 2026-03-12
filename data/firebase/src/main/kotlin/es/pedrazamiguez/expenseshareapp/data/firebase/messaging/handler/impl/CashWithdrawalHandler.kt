package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.formatNotificationAmount
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.stableNotificationId
import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class CashWithdrawalHandler(
    private val context: Context,
    private val localeProvider: LocaleProvider
) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val amount = formatNotificationAmount(data, localeProvider)
        val groupName = data["groupName"] ?: ""
        val groupId = data["groupId"]
        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_cash_withdrawal_title)
            },
            body = context.getString(
                R.string.notification_cash_withdrawal_body,
                memberName,
                amount
            ),
            deepLink = data["deepLink"],
            channelId = NotificationChannelId.FINANCIAL,
            groupId = groupId,
            notificationId = stableNotificationId("CASH_WITHDRAWAL", groupId, data["entityId"])
        )
    }
}

