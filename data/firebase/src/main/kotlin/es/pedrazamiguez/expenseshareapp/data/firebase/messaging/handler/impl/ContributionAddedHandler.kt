package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.formatNotificationAmount
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class ContributionAddedHandler(
    private val context: Context,
    private val localeProvider: LocaleProvider
) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val amount = formatNotificationAmount(data, localeProvider)
        val groupName = data["groupName"] ?: ""
        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_contribution_added_title)
            },
            body = context.getString(
                R.string.notification_contribution_added_body,
                memberName,
                amount
            ),
            deepLink = data["deepLink"]
        )
    }
}

