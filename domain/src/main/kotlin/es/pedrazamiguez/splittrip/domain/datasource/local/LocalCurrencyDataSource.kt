package es.pedrazamiguez.splittrip.domain.datasource.local

import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.model.ExchangeRates

interface LocalCurrencyDataSource {
    // Currencies
    suspend fun saveCurrencies(currencies: List<Currency>)
    suspend fun getCurrencies(): List<Currency>

    // Exchange rates
    suspend fun saveExchangeRates(rates: ExchangeRates)
    suspend fun getExchangeRates(base: String): ExchangeRates
    suspend fun getLastUpdated(base: String): Long?
}
