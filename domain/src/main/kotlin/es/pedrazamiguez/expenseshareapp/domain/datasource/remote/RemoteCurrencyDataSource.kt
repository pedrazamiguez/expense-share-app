package es.pedrazamiguez.expenseshareapp.domain.datasource.remote

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates

interface RemoteCurrencyDataSource {
    suspend fun fetchCurrencies(): List<Currency>
    suspend fun fetchExchangeRates(baseCurrencyCode: String): ExchangeRates
}
