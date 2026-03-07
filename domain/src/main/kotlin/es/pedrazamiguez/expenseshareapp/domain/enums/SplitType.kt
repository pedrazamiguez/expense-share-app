package es.pedrazamiguez.expenseshareapp.domain.enums

enum class SplitType {
    EQUAL,
    EXACT,
    PERCENT;

    companion object {
        fun fromString(value: String): SplitType {
            return entries.find {
                it.name.equals(value, ignoreCase = true)
            } ?: throw IllegalArgumentException("Unknown SplitType: $value")
        }
    }
}

