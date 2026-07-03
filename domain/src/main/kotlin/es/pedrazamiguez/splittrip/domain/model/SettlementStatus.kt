package es.pedrazamiguez.splittrip.domain.model

enum class SettlementStatus {
    SUGGESTED,
    CONFIRMED_BY_PAYER,
    CONFIRMED_BY_BOTH,
    DISPUTED,
    RESOLVED;

    companion object {
        fun fromString(s: String?): SettlementStatus =
            entries.find { it.name.equals(s, ignoreCase = true) } ?: SUGGESTED
    }
}
