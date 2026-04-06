package es.pedrazamiguez.splittrip.domain.enums

/**
 * Determines how an add-on relates to the parent expense's base amount.
 *
 * - [ON_TOP]: The add-on amount is ADDED to the base amount (e.g., ATM fee, tip on top, surcharge).
 *   The effective group total grows by the add-on's groupAmountCents.
 *
 * - [INCLUDED]: The add-on is EXTRACTED from the user-entered total (e.g., "total already includes 10% tip").
 *   The expense's `sourceAmount`/`groupAmount` store the **base cost** (total minus included portions),
 *   and the add-on's `groupAmountCents` captures the extracted portion. The effective group total is
 *   reconstructed as `baseCost + INCLUDED add-ons`, which equals the original user-entered total.
 *   Both ON_TOP and INCLUDED decompose the payment into **base + add-on**; the only difference is the
 *   input flow: ON_TOP adds on top of the base, INCLUDED extracts from the total to derive the base.
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
