package es.pedrazamiguez.expenseshareapp.domain.enums

enum class NotificationType {
    EXPENSE_ADDED, GROUP_INVITE, SETTLEMENT_REQUEST, DEFAULT;

    companion object {
        fun fromString(type: String?): NotificationType {
            return entries.find {
                it.name.equals(
                    type,
                    ignoreCase = true
                )
            } ?: DEFAULT
        }
    }
}
