package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * User-configurable notification preferences.
 *
 * Each boolean maps to one of the notification category groups.
 * When disabled, the client will suppress notifications in that
 * category before displaying them.
 */
data class NotificationPreferences(
    val membershipEnabled: Boolean = true,
    val expensesEnabled: Boolean = true,
    val financialEnabled: Boolean = true
)

