package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.data.firebase.R
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class ExpenseAddedHandler(private val context: Context) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        val amount = data["amount"] ?: "0"
        return NotificationContent(
            title = context.getString(R.string.notification_expense_added_title),
            body = context.getString(
                R.string.notification_expense_added_body,
                amount
            )
        )
    }
}
