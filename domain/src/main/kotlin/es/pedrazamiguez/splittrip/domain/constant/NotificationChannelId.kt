package es.pedrazamiguez.splittrip.domain.constant

/**
 * Notification channel IDs used to categorize notifications.
 *
 * Each channel maps to a user-visible notification category in Android settings.
 * Handlers populate [NotificationContent.channelId] with one of these constants
 * so the messaging service routes the notification to the correct channel.
 */
object NotificationChannelId {
    /** Membership activity: member joined, left, invitations. */
    const val MEMBERSHIP = "splittrip_membership"

    /** Expense activity: added, updated, deleted. */
    const val EXPENSES = "splittrip_expenses"

    /** Financial activity: cash withdrawals, contributions. */
    const val FINANCIAL = "splittrip_financial"

    /** Fallback channel for unknown / default notification types. */
    const val DEFAULT = "splittrip_updates"
}
