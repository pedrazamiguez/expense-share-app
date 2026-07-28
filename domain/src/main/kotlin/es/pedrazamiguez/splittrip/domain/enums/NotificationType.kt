package es.pedrazamiguez.splittrip.domain.enums

enum class NotificationType {
    EXPENSE_ADDED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    EXPENSE_SCHEDULED_REMINDER,
    EXPENSE_SCHEDULED_EFFECTIVE,
    EXPENSE_REFUNDABLE_REMINDER,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    CASH_WITHDRAWAL,
    CONTRIBUTION_ADDED,
    GROUP_DELETED,
    GROUP_INVITE,
    SETTLEMENT_REQUEST,
    SETTLEMENT_CONFIRMED,
    SETTLEMENT_DISPUTED,
    DEFAULT;

    /**
     * Maps this notification type to the user-facing preference category.
     * Returns `null` for [DEFAULT] since it should always be shown.
     */
    fun toCategory(): NotificationCategory? = when (this) {
        EXPENSE_ADDED,
        EXPENSE_UPDATED,
        EXPENSE_DELETED,
        EXPENSE_SCHEDULED_REMINDER,
        EXPENSE_SCHEDULED_EFFECTIVE,
        EXPENSE_REFUNDABLE_REMINDER -> NotificationCategory.EXPENSES
        MEMBER_ADDED, MEMBER_REMOVED, GROUP_DELETED, GROUP_INVITE -> NotificationCategory.MEMBERSHIP
        CASH_WITHDRAWAL,
        CONTRIBUTION_ADDED,
        SETTLEMENT_REQUEST,
        SETTLEMENT_CONFIRMED,
        SETTLEMENT_DISPUTED -> NotificationCategory.FINANCIAL
        DEFAULT -> null
    }

    companion object {
        fun fromString(type: String?): NotificationType = entries.find {
            it.name.equals(
                type,
                ignoreCase = true
            )
        } ?: DEFAULT
    }
}
