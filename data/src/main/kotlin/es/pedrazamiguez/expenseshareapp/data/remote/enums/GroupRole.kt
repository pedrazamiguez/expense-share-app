package es.pedrazamiguez.expenseshareapp.data.remote.enums

enum class GroupRole {
    ADMIN, MEMBER, GUEST;

    companion object {
        fun fromString(role: String): GroupRole {
            return entries.find { it.name.equals(role, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown role: $role")
        }
    }
}
