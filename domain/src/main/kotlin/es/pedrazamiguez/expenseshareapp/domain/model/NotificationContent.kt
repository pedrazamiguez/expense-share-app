package es.pedrazamiguez.expenseshareapp.domain.model

data class NotificationContent(
    val title: String, val body: String, val deepLink: String? = null
)
