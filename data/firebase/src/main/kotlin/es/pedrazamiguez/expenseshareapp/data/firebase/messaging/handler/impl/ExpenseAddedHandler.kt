package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.impl

import android.content.Context
import es.pedrazamiguez.expenseshareapp.domain.handler.NotificationHandler
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

class ExpenseAddedHandler(private val context: Context) : NotificationHandler {
    override fun handle(data: Map<String, String>): NotificationContent {
        // Lógica para extraer datos y formatear string
        val amount = data["amount"] ?: "0"
        // ... obtener recursos usando context.getString(...)
        return NotificationContent(
            title = "Nuevo Gasto", // Usar R.string real
            body = "Se ha añadido un gasto de $amount"
        )
    }
}
