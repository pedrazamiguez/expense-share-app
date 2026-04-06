package es.pedrazamiguez.splittrip.domain.enums

enum class NotificationType {
    EXPENSE_ADDED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    CASH_WITHDRAWAL,
    CONTRIBUTION_ADDED,
    GROUP_DELETED,
    GROUP_INVITE,
    SETTLEMENT_REQUEST,
    DEFAULT;

    /**
     * Maps this notification type to the user-facing preference category.
     * Returns `null` for [DEFAULT] since it should always be shown.
     */
    fun toCategory(): NotificationCategory? = when (this) {
        EXPENSE_ADDED, EXPENSE_UPDATED, EXPENSE_DELETED -> NotificationCategory.EXPENSES
        MEMBER_ADDED, MEMBER_REMOVED, GROUP_DELETED, GROUP_INVITE -> NotificationCategory.MEMBERSHIP
        CASH_WITHDRAWAL, CONTRIBUTION_ADDED, SETTLEMENT_REQUEST -> NotificationCategory.FINANCIAL
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
