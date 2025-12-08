package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class DefaultHandler(private val context: Context) : NotificationHandler {

    override fun handle(
        data: Map<String, String>
    ): NotificationContent {
        val title = data["title"] ?: "Expense Share App"
//        val body = data["body"] ?: context.getString(R.string.notification_new_message)
        val body = data["body"] ?: "You have a new notification."
        return NotificationContent(
            title = title,
            body = body
        )
    }

}
