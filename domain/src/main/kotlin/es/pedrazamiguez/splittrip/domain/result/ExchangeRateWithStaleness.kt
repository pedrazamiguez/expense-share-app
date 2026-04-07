package es.pedrazamiguez.splittrip.domain.result

import java.math.BigDecimal
import java.time.Instant

/**
 * Wraps an exchange rate with staleness metadata.
 *
 * Returned by [es.pedrazamiguez.splittrip.domain.usecase.currency.GetExchangeRateUseCase]
 * so that callers can display a warning when the cached rate is outdated
 * (e.g., the device has been offline and the API could not be reached).
 *
 * @param rate The calculated cross-rate (1 base = X target).
 * @param isStale True when the rate was served from an expired local cache
 *        because the remote API was unreachable.
 * @param lastUpdated Timestamp of the last successful rate fetch, or null if unknown.
 */
data class ExchangeRateWithStaleness(
    val rate: BigDecimal,
    val isStale: Boolean,
    val lastUpdated: Instant? = null
)
