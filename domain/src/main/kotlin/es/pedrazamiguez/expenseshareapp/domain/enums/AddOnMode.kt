package es.pedrazamiguez.expenseshareapp.domain.enums

/**
 * Determines how an add-on relates to the parent expense's base amount.
 *
 * - [ON_TOP]: The add-on amount is ADDED to the base amount (e.g., ATM fee, tip on top, surcharge).
 *   The effective group total grows by the add-on's groupAmountCents.
 *
 * - [INCLUDED]: The add-on is EXTRACTED from the base amount (e.g., "total already includes 10% tip").
 *   The expense total does NOT change — the add-on is purely informational for analytics
 *   ("how much did we spend on tips?").
 */
enum class AddOnMode {
    ON_TOP,
    INCLUDED;

    companion object {
        fun fromString(mode: String): AddOnMode = entries.find {
            it.name.equals(mode, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unknown add-on mode: $mode")
    }
}
