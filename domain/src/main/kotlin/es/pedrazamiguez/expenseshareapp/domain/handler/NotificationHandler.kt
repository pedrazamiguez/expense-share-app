package es.pedrazamiguez.expenseshareapp.domain.handler

import es.pedrazamiguez.expenseshareapp.domain.model.NotificationContent

/**
 * Handler for processing notification data and creating notification content.
 * Implementations should extract relevant information from the notification data map
 * and format it into user-friendly notification content.
 */
interface NotificationHandler {
    /**
     * Processes notification data and returns formatted notification content.
     * @param data Map of key-value pairs from the remote message
     * @return NotificationContent with formatted title and body
     */
    fun handle(data: Map<String, String>): NotificationContent
}
