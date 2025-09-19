package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates

interface LocalCurrencyDataSource {
    // Currencies
    suspend fun saveCurrencies(currencies: List<Currency>)
    suspend fun getCurrencies(): List<Currency>

    // Exchange rates
    suspend fun saveExchangeRates(rates: ExchangeRates)
    suspend fun getExchangeRates(base: String): ExchangeRates
    suspend fun getLastUpdated(base: String): Long?
}
