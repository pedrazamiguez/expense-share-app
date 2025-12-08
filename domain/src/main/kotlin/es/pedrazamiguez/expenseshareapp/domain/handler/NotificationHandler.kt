package es.pedrazamiguez.expenseshareapp.domain.handler

import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

interface NotificationHandler {
    fun handle(data: Map<String, String>): NotificationContent
}
