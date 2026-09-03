package es.pedrazamiguez.splittrip.domain.enums

enum class SubscriptionTier {
    FREE,
    PRO;

    companion object {
        fun fromString(tier: String): SubscriptionTier =
            entries.find { it.name.equals(tier, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown subscription tier: $tier")

        fun fromStringOrDefault(tier: String?): SubscriptionTier =
            tier?.let { t -> entries.find { it.name.equals(t, ignoreCase = true) } } ?: FREE
    }
}
