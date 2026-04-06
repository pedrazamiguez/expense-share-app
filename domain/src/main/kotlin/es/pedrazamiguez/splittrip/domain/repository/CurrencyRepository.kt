package es.pedrazamiguez.splittrip.domain.repository

import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.result.ExchangeRateResult

interface CurrencyRepository {
    suspend fun getCurrencies(forceRefresh: Boolean = false): List<Currency>
    suspend fun getExchangeRates(baseCurrencyCode: String): ExchangeRateResult
}
