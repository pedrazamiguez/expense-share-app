package es.pedrazamiguez.expenseshareapp.domain.model

import es.pedrazamiguez.expenseshareapp.domain.constant.NotificationChannelId

data class NotificationContent(
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val channelId: String = NotificationChannelId.DEFAULT,
    val groupId: String? = null,
    val notificationId: Int = 0
)
