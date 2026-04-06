package es.pedrazamiguez.splittrip.domain.enums

enum class SplitType {
    EQUAL,
    EXACT,
    PERCENT;

    companion object {
        fun fromString(value: String): SplitType = entries.find {
            it.name.equals(value, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unknown SplitType: $value")
    }
}
