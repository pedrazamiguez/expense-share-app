package es.pedrazamiguez.expenseshareapp.data.remote.enums

enum class ActivityType {
    GROUP_CREATED, GROUP_UPDATED, GROUP_DELETED, MEMBER_ADDED, MEMBER_REMOVED, SUBGROUP_CREATED, SUBGROUP_UPDATED, SUBGROUP_DELETED, EXPENSE_CREATED, EXPENSE_UPDATED, EXPENSE_DELETED, UNKNOWN;

    companion object {
        fun fromString(type: String): ActivityType {
            return entries.find { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown activity type: $type")
        }
    }
}
