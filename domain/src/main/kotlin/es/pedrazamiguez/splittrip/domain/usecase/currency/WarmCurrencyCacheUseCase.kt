package es.pedrazamiguez.splittrip.domain.usecase.currency

import es.pedrazamiguez.splittrip.domain.repository.CurrencyRepository

/**
 * Fire-and-forget use case that prefetches currency reference data into the
 * local Room cache while the user has connectivity.
 *
 * Designed to run during post-login initialisation so that the currency list
 * and USD-base exchange rates are available offline on subsequent uses —
 * critical for first-time users who sign up at home and then go offline
 * while travelling.
 *
 * Both calls are independent: a failure in one does not prevent the other from
 * executing. Failures are silently swallowed — this is purely opportunistic and
 * must never block the UI. The caller is responsible for logging if needed.
 *
 * - `getCurrencies(false)` only fetches from the network if Room is empty.
 * - `getExchangeRates("USD")` only fetches if the cache is empty or stale.
 */
class WarmCurrencyCacheUseCase(private val currencyRepository: CurrencyRepository) {

    /**
     * USD base code — the only base currency supported by the
     * OpenExchangeRates Free Tier.
     */
    companion object {
        private const val USD_BASE = "USD"
    }

    suspend operator fun invoke() {
        runCatching { currencyRepository.getCurrencies(forceRefresh = false) }
        runCatching { currencyRepository.getExchangeRates(USD_BASE) }
    }
}
