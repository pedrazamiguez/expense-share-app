package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory

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
) {
    /**
     * Returns whether the given [category] is enabled.
     */
    fun isCategoryEnabled(category: NotificationCategory): Boolean = when (category) {
        NotificationCategory.MEMBERSHIP -> membershipEnabled
        NotificationCategory.EXPENSES -> expensesEnabled
        NotificationCategory.FINANCIAL -> financialEnabled
    }
}
