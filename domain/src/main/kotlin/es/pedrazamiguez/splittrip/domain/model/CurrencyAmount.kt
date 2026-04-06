package es.pedrazamiguez.splittrip.domain.model

/**
 * Represents a monetary amount in a specific currency with its group-currency equivalent.
 *
 * Used for per-member per-currency breakdowns (cash in hand, expenses by currency).
 *
 * @param currency ISO 4217 currency code (e.g., "THB", "EUR").
 * @param amountCents Amount in the source currency's minor units (cents).
 * @param equivalentCents Converted amount in the group's base currency (cents).
 *                        When [currency] is the group currency, equals [amountCents].
 */
data class CurrencyAmount(
    val currency: String,
    val amountCents: Long,
    val equivalentCents: Long
)
