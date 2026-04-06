package es.pedrazamiguez.splittrip.domain.enums

/**
 * Determines how the user originally specified the add-on value.
 *
 * - [EXACT]: The user entered an absolute amount (e.g., "2.50 EUR").
 * - [PERCENTAGE]: The user entered a percentage of the base amount (e.g., "10%").
 *
 * In both cases the resolved absolute amount is always stored in [AddOn.amountCents].
 * This enum is metadata for display/edit purposes only — it does NOT affect calculations.
 */
enum class AddOnValueType {
    EXACT,
    PERCENTAGE;

    companion object {
        fun fromString(value: String): AddOnValueType = entries.find {
            it.name.equals(value, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unknown add-on value type: $value")
    }
}
