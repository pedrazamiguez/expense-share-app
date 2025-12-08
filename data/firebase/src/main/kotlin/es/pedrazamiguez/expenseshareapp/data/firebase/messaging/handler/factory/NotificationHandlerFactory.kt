package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.factory

import android.content.Context
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.DefaultHandler
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl.ExpenseAddedHandler
import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationType
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler

class NotificationHandlerFactory(private val context: Context) {

    fun getHandler(type: NotificationType): NotificationHandler {
        return when (type) {
            NotificationType.EXPENSE_ADDED -> ExpenseAddedHandler(context)
            else -> DefaultHandler(context)
        }
    }
}
