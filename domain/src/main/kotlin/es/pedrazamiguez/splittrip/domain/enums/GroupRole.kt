package es.pedrazamiguez.splittrip.domain.enums

enum class GroupRole {
    ADMIN,
    MEMBER,
    GUEST;

    companion object {
        fun fromString(role: String): GroupRole = entries.find {
            it.name.equals(
                role,
                ignoreCase = true
            )
        } ?: throw IllegalArgumentException("Unknown role: $role")
    }
}
