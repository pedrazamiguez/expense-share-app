package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.constant.NotificationChannelId

/**
 * @property notificationId Stable ID used with NotificationManager.notify.
 *   Handlers should always set this explicitly via a notification ID generator. The fallback
 *   derives an ID from [title] and [body] so that an unset value never silently
 *   overwrites other notifications at ID 0.
 */
data class NotificationContent(
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val channelId: String = NotificationChannelId.DEFAULT,
    val groupId: String? = null,
    val notificationId: Int = "$title|$body".hashCode()
)
