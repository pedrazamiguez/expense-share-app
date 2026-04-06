package es.pedrazamiguez.splittrip.domain.datasource.remote

import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.model.ExchangeRates

interface RemoteCurrencyDataSource {
    suspend fun fetchCurrencies(): List<Currency>
    suspend fun fetchExchangeRates(baseCurrencyCode: String): ExchangeRates
}
