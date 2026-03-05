package es.pedrazamiguez.expenseshareapp.domain.exception

/**
 * Thrown when a cash expense cannot be covered by the available cash withdrawals.
 *
 * Contains the raw cent values so the presentation layer can format them correctly
 * using locale-aware formatting and the proper currency symbol — never format here.
 *
 * @param requiredCents  The expense amount in smallest currency units (e.g. cents for EUR).
 * @param availableCents The total remaining cash in smallest currency units.
 */
class InsufficientCashException(
    val requiredCents: Long,
    val availableCents: Long
) : Exception("Insufficient cash to cover the expense.")

