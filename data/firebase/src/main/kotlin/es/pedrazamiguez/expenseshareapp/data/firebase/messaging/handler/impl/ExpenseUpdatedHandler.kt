package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class ExpenseUpdatedHandler(private val context: Context) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val memberName = data["memberName"] ?: "Someone"
        val amount = data["amount"] ?: ""
        val groupName = data["groupName"] ?: ""
        return NotificationContent(
            title = groupName.ifBlank {
                context.getString(R.string.notification_expense_updated_title)
            },
            body = context.getString(
                R.string.notification_expense_updated_body,
                memberName,
                amount
            ),
            deepLink = data["deepLink"]
        )
    }
}

