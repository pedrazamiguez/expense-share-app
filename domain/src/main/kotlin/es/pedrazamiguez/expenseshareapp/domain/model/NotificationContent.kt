package es.pedrazamiguez.expenseshareapp.domain.model

data class NotificationContent(
    val title: String,
    val body: String,
    // TODO: Implement deep link handling in ExpenseShareMessagingService.showNotification()
    // to navigate to specific screens when users tap on notifications using this field.
    val deepLink: String? = null
)
